import RPi.GPIO as GPIO
import time
import Adafruit_ADS1x15
import math
 
GPIO.setmode(GPIO.BCM)
GPIO.setup(23,GPIO.IN)
 
for i in range(0,5):
    print(GPIO.input(23))

adc =Adafruit_ADS1x15.ADS1115()
GAIN = 1
PIN=7
def setup():
    #GPIO.setmode(GPIO.BOARD)
    GPIO.setup(PIN, GPIO.OUT)
    GPIO.setup(18, GPIO.OUT)
    GPIO.setup(25, GPIO.OUT)
    GPIO.output(PIN, GPIO.HIGH)
    time.sleep(0.1)

values = [0]*100

def startPump():
    GPIO.output(PIN, GPIO.LOW)
    print("PUMP ON")
    
    
def stopPump():
    GPIO.output(PIN, GPIO.HIGH)
    print("PUMP OFF")
    
def dispense(t = 1):
    GPIO.output(PIN, GPIO.LOW)
    print("DISPENSE")
    time.sleep(t)
    GPIO.output(PIN, GPIO.HIGH)

def light(light_value):
    if light_value:
        GPIO.output(18, True)
    else:
        GPIO.output(18, False)

def soilmoist():
    # soil moisture on port 0
    return adc.read_adc(0, gain=GAIN)

def lightsensor():
    return adc.read_adc(1, gain=2)

def fan(fan_value):
    if fan_value:
        GPIO.output(25, True)
    else:
        GPIO.output(25, False)

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
    GPIO.output(PIN, GPIO.HIGH)
    GPIO.cleanup()

if __name__ == '__main__':
    setup()
    try:
        loop()
    except KeyboardInterrupt:
        destroy()