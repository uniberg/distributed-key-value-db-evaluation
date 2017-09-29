#!/bin/bash

for dc in "dc1" "dc2"; do
  for node in "1" "2" "3"; do
    ssh pp1${dc}-${node} "sudo pkill redis" &
  done
done
