#!/bin/bash


# Change this to your netid
netid=rxd170930

#
# Root directory of your project
PROJDIR=/home/012/r/rx/rxd170930/AOS/lamport-mutex-algo

#
# Directory where the config file is located on your local system
CONFIGLOCAL=config.txt

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read -r firstline
    array=( $firstline )
    i=${array[0]}
    echo $i
    while [[ $n -lt $i ]]
    do
    	read line
        host=$( echo $line | awk '{ print $2 }' )

        echo $host
        gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host killall -u $netid" &
        sleep 1

        n=$(( n + 1 ))
    done
   
)


echo "Cleanup complete"
