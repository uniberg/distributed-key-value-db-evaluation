#!/bin/bash
command="sudo docker stop db && sudo docker rm db && sudo docker run -d --name db -v ~/couchbase:/opt/couchbase/var --net=host couchbase"

ssh pp1dc1-1 "$command" &
ssh pp1dc1-2 "$command" &
ssh pp1dc1-3 "$command" &
ssh pp1dc2-1 "$command" &
ssh pp1dc2-2 "$command" &
ssh pp1dc2-3 "$command" &
