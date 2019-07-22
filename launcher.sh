#!/bin/bash

# Change this to your netid
netid=rxd170930

# Root directory of your project
PROJDIR=/home/012/r/rx/rxd170930/AOS/AOS_Project2

# Directory where the config file is located on your local system
CONFIGLOCAL=config.txt

# Directory your java classes are in
BINDIR=$PROJDIR/out/production/AOS_Project2

# Your main project class
PROG=Main

n=0

cat $CONFIGLOCAL | sed -e "s/#.*//" | sed -e "/^\s*$/d" |
(
    read -r firstline
    array=( $firstline )
    i=${array[0]}
    while [[ $n -lt $i ]]
    do
    	read line
    	p=$( echo $line | awk '{ print $1 }' )
        host=$( echo $line | awk '{ print $2 }' )
#        if [ $n -eq $((i-1)) ]
#            then
#                state="a"
#        fi
	gnome-terminal -e "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $netid@$host java -cp $BINDIR $PROG; exec bash" &

        n=$(( n + 1 ))
    done
)
