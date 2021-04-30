#!/usr/bin/env bash

echo "Saving 5 second video to /home/pi/camera/"

DATE=(date +"%Y-%m-%d_%H%M")

raspivid -o /home/pi/camera/$DATE.h264 -t 5000
