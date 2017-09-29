#!/bin/bash

ssh pp1dc1-1 "sudo docker stop db" &
ssh pp1dc1-2 "sudo docker stop db" &
ssh pp1dc1-3 "sudo docker stop db" &
ssh pp1dc2-1 "sudo docker stop db" &
ssh pp1dc2-2 "sudo docker stop db" &
ssh pp1dc2-3 "sudo docker stop db" &
