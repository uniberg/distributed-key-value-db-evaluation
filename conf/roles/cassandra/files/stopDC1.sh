#!/bin/bash

pid1=`ssh pp1dc1-1 "pgrep java"`
pid2=`ssh pp1dc1-2 "pgrep java"`
pid3=`ssh pp1dc1-3 "pgrep java"`
echo $pid1 $pid2 $pid3
ssh pp1dc1-1 "sudo kill -9 $pid1" &
ssh pp1dc1-2 "sudo kill -9 $pid2" &
ssh pp1dc1-3 "sudo kill -9 $pid3" &
