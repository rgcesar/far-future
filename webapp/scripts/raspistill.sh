#!/usr/bin/env bash

echo "Saving picture to /home/pi/camera/"

DATE=(date +"%Y-%m-%d_%H%M")

raspistill -o /home/pi/camera/$DATE.jpg