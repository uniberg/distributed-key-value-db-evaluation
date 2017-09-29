#!/bin/bash

ssh pp1dc1-1 "sudo docker kill db" &
ssh pp1dc1-2 "sudo docker kill db" &
ssh pp1dc1-3 "sudo docker kill db" &
