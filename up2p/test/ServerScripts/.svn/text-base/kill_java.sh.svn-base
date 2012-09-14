#!/bin/bash
# Kills all currently running java processes
# Alexander Craig July 2009

echo "Killing all running java processes"
echo "$(ps -e)" > temp.out
exec < temp.out
while read line
do
  case $line in 
      *java)
	  idLength=$(expr index "$line" " ")
	  id=${line:0:$idLength}
	  echo "Killing java process, id: " $id
	  $(kill -9 $id);;
  esac
done
$(rm -f temp.out)
exit
