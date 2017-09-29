#!/bin/bash

# Management Node has no Settings
ISMGMT=`hostname |grep mgmt |wc -l`
if [ "$ISMGMT" == "1" ]; then
    exit
fi

# If the settings are already active, don't apply them again
SHOWLINES=`sudo tc qdisc show dev ens3 |wc -l`
if [ "$SHOWLINES" != "1" ]; then
    exit
fi

# Define which hostname is in use
ISDC1=`hostname |grep dc1 |wc -l`
HOSTNAMEPART="pp1dc1"
if [ "$ISDC1" == "1" ]; then
    HOSTNAMEPART="pp1dc2"
fi

# Set the Delay to 10 +-1 ms
tc qdisc add dev ens3 root handle 1: prio
tc qdisc add dev ens3 parent 1:1 handle 30: netem delay 5ms 0.5ms distribution normal
tc qdisc add dev ens3 parent 30:1 handle 31: netem loss 0.01%
tc qdisc add dev ens3 parent 31:1 handle 32: cbq bandwidth 1gbit rate 1gbit avpkt 1000
for ip in `grep $HOSTNAMEPART /etc/hosts |cut -d ' ' -f 1`; do
    echo $ip
    tc filter add dev ens3 protocol ip parent 1:0 prio 3 u32 match ip dst ${ip}/32 flowid 31:1
done
echo "Delay and Packet Loss set"

#remove the settings
#sudo tc qdisc del dev ens3 root
