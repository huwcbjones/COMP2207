#!/bin/bash
for file in "$@"
do
  host=$(echo $file | sed -E 's/\.(4|6)\.csv//g' | sed -E 's/.*\///g')
  awk '
  BEGIN {
    RS = "\n";
    FS = ",";
    recordNum = 0;
    average = 0;
  }
  {
    if(recordNum != 0){
      average=average + $4
    }
    recordNum++;
  }
  END {
    printf("'$host',%.3f\n",(average/recordNum));
  }
  ' $file
done
