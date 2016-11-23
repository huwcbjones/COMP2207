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
    averageNum = 0;
    no_route = 0;
    blocked = 0;
    status = "success";
  }
  {
    if(recordNum != 0){
      if($4 == " 0 received") {
        blocked++;
      } else if ($4 == "") {
        no_route++;
      } else {
        average=average + $4;
        averageNum++;
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
    recordNum--;
    if (blocked == recordNum) {
      printf("'$host',,,,%s\n", "blocked");
    } else if (no_route == recordNum) {
      printf("'$host',,,,%s\n", "no_route");
    } else {
      printf("'$host',%.3f,%.3f,%.3f,%s\n",min,maxi,(average/averageNum), status);
    }
  }
  ' $file
done
