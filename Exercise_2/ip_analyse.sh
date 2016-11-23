#!/bin/bash

# Number of pings
COUNT=30

# Interval between pings
INTERVAL=0.5

# Output extension
OUTPUT_EXT=".csv"
ping6=false
file=''

while getopts 'c:i:f:6' flag; do
  case "${flag}" in
    6) ping6=true ;;
    c) COUNT="${OPTARG}" ;;
    i) INTERVAL="${OPTARG}" ;;
    f) file="${OPTARG}" ;;
    *) echo "Unexpected option ${flag}" ; exit ;
  esac
done



# Prints out a progress bar
# @param $1 Current iteration number
# @param $2 Number of iterations
function printProgress() {
	awk '
	BEGIN {
		percentage = ( '$1' / '$2');
		numberHashes = ( percentage * 50 );
		hashString = "";
		for(i = 1; i < numberHashes; i++){
			hashString = hashString "#";
		}
		printf("\rProgress [%-50s] (%.2f%s)\t\t%-50s", hashString, ( percentage * 100 ), "%", "'$3'");
	}'
}
function getIP() {
  if [ "$ping6" = true ]; then
    result=$(ping6 -c 1 -q $1)
  else
    result=$(ping -c 1 -q $1)
  fi
  echo -e "$result" | head -n 1 | sed -E "s/\).*//g" | sed -E "s/.*\(//g"
}

function getPingResult() {
  if [ "$ping6" = true ]; then
    result=$(ping6 -c $COUNT -i $INTERVAL -q $1)
  else
    result=$(ping -c $COUNT -i $INTERVAL -q $1)
  fi

  echo -e "$result" | tail -n 1 | \
    sed -e 's/.*=[^0-9]//g' -e 's/[^0-9]*ms//g' -e 's/\//,/g'
}

function getHosts(){
  awk -F "," '{print $2}' "$file"
}

HOSTS="$(getHosts $1)"
hostCount=$(echo "$HOSTS" | wc -l)
counter=0

# Create output directory
mkdir -p "output"

echo -ne "Creating files...\n"
while read -r host; do
  printProgress $counter $hostCount $host
  if [ ! -e "output/"$host.6$OUTPUT_EXT ]; then
    echo "time,ip,min,avg,max,stddev" > "output/"$host.6$OUTPUT_EXT
  fi
  if [ ! -e "output/"$host.4$OUTPUT_EXT ]; then
    echo "time,ip,min,avg,max,stddev" > "output/"$host.4$OUTPUT_EXT
  fi
  counter=$((counter+1))
done <<< "$HOSTS"
printProgress $counter $hostCount ""

echo -ne "\nPinging hosts...\n"
counter=0
printProgress $counter $hostCount;
while read -r host; do
  printProgress $(($counter)) $hostCount $host;
  if [ "$ping6" = true ]; then
    echo -e "$(date +"%Y-%m-%d %H:%M:%S"),$(getIP $host),$(getPingResult $host)" >> "output/"$host.6$OUTPUT_EXT;
  else
    echo -e "$(date +"%Y-%m-%d %H:%M:%S"),$(getIP $host),$(getPingResult $host)" >> "output/"$host.4$OUTPUT_EXT;
  fi;
  counter=$((counter+1));
done <<< "$HOSTS"
printProgress $counter $hostCount ""
