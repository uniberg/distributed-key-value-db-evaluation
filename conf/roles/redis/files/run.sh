#!/bin/bash

redisinstances=`pgrep redis |wc -l`
if [ "$redisinstances" == "0" ]; then
    sysctl vm.overcommit_memory=1
    echo never > /sys/kernel/mm/transparent_hugepage/enabled
    hostname=`hostname`
#    if [ "$hostname" == "pp1dc1-1" ]; then
#        /opt/redis/src/redis-server /opt/redis/redis_master.conf
#    else
#        /opt/redis/src/redis-server /opt/redis/redis_slave.conf
#    fi
    /opt/redis/src/redis-server /opt/redis/redis_cluster.conf
fi
