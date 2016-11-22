#!/bin/bash
for file in "$@"
do
  host=$(echo $file | sed -E 's/\.(4|6)\.csv//g' | sed -E 's/.*\///g')
  awk '
  BEGIN {
    RS = "\n";
    FS = ",";
    recordNum = 0;
    min = 9999999;
    maxi = 0;
    average = 0;
    status = "success";
  }
  {
    if(recordNum != 0){
      if($4 == " 0 received") {
        status = "Blocked";
      } else if ($4 == "") {
        status = "No route to host";
      } else {
        average=average + $4;
        if($3 < min){
          min = $3
        }
        if($5 > maxi){
          maxi = $5
        }
      }
    }
    recordNum++;
  }
  END {
    if(status != "success") {
      printf("'$host',,,,%s\n", status);
    } else {
      printf("'$host',%.3f,%.3f,%.3f,%s\n",min,maxi,(average/recordNum), status);
    }
  }
  ' $file
done
