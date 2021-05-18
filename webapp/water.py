import RPi.GPIO as GPIO
import time
#import Adafruit_ADS1x15
#import adafruit_ads1x15.ads1115 as ADS
import board
import busio
#i2c = busio.I2C(board.SCL, board.SDA)

import math
 
GPIO.setmode(GPIO.BCM)
GPIO.setup(23,GPIO.IN)
 
#for i in range(0,5):
#    print(GPIO.input(23))

#adc = Adafruit_ADS1x15.ADS1115()
#adc = ADS.ADS1115(i2c)
#adc.mode = Mode.CONTINUOUS
GAIN = 1
pump_pin=4
fan_pin = 13
light_pin = 17
def setup():
    #GPIO.setmode(GPIO.BOARD)
    GPIO.setup(pump_pin, GPIO.OUT)
    GPIO.setup(light_pin, GPIO.OUT)
    GPIO.setup((fan_pin), GPIO.OUT)
    GPIO.output(pump_pin, GPIO.HIGH)
    time.sleep(0.1)

values = [0]*100

def startPump():
    GPIO.output(pump_pin, GPIO.LOW)
    print("PUMP ON")
    
    
def stopPump():
    GPIO.output(pump_pin, GPIO.HIGH)
    print("PUMP OFF")
    
def dispense(t = 1):
    GPIO.output(pump_pin, GPIO.LOW)
    print("DISPENSE")
    time.sleep(t)
    GPIO.output(pump_pin, GPIO.HIGH)

def light(light_value):
    if light_value:
        GPIO.output(light_pin, True)
    else:
        GPIO.output(light_pin, False)

def soilmoist():
    # soil moisture on port 0
    #return 100 - (100 * (adc.read_adc(0, gain=1) / 32767))
    return 50

def lightsensor():
    #return 100 - (100 * (adc.read_adc(1, gain=1) / 32767))
    return 50

def waterlevel():
    #return adc.read_adc(2, gain=2)
    return 50

def fan(fan_value):
    if fan_value:
        GPIO.output(fan_pin, True)
    else:
        GPIO.output(fan_pin, False)

def loop():
    #while True:
    #    for i in range(100):
    #        values[i] = adc.read_adc(0, gain=GAIN)
    #    print(max(values))
    #    if (max(values))>20000:
    #        GPIO.output(PIN, GPIO.LOW)
    #        print("ON")
    #        print(PIN)
    #        time.sleep(0.1)
    #    else:
    #       GPIO.output(PIN, GPIO.HIGH)
    #       print("OFF")
    #       print(PIN)
    #       time.sleep(0.1)
    dispense(1)
    destroy()

def destroy():
    GPIO.output(pump_pin, GPIO.HIGH)
    GPIO.cleanup()

if __name__ == '__main__':
    setup()
    try:
        #loop()
        #print(waterlevel())
        dispense(7)
        #light(1)
        #xfan(1)
    except KeyboardInterrupt:
        destroy()