#!/bin/bash

pid1=`ssh pp1dc2-1 "pgrep redis"`
pid2=`ssh pp1dc2-2 "pgrep redis"`
pid3=`ssh pp1dc2-3 "pgrep redis"`
ssh pp1dc2-1 "sudo kill -9 $pid1" &
ssh pp1dc2-2 "sudo kill -9 $pid2" &
ssh pp1dc2-3 "sudo kill -9 $pid3" &
