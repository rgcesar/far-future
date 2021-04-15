#!/usr/bin/python

from flask import request
from flask_api import FlaskAPI
import RPi.GPIO as GPIO

LEDS = {"green": 8, "red": 10}
GPIO.setmode(GPIO.BOARD)
GPIO.setup(LEDS["green"], GPIO.OUT)
GPIO.setup(LEDS["red"], GPIO.OUT)

app = FlaskAPI(__name__)

@app.route('/', methods=["GET"])
def api_root():
    return {
           "led_url": request.url + "led/(green | red)/",
             "led_url_POST": {"state": "(0 | 1)"}
                 }
  
@app.route('/led/<color>/', methods=["GET", "POST"])
def api_leds_control(color):
    if request.method == "POST":
        if color in LEDS:
            GPIO.output(LEDS[color], int(request.data.get("state")))
    return {color: GPIO.input(LEDS[color])}

if __name__ == "__main__":
    app.run()
