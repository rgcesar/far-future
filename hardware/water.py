import RPi.GPIO as GPIO
import time
import Adafruit_ADS1x15
import math


''' instantiate ADS1115 module to read sensor data later
    define position variable gain
    define physical pin of Raspberry Pi '''

adc = Adafruit_ADS1x15.ADS1115()
GAIN = 1
PIN = 7


# value of the sensor that controls water flow
sensor_val = 20000


''' Set the 7-pin mode, pin input/output mode,
    initialization pin and delay 0.1 second debounce
    in the setup() function '''

def setup():
    GPIO.setmode(GPIO.BOARD)
    GPIO.setup(PIN, GPIO.OUT)
    GPIO.output(PIN, GPIO.HIGH)
    time.sleep(0.1)
    
    
''' implement specific function in the loop() function
    if read value is larger than sensor_val, the relay
    is operated by the pump. '''

values = [0]*100

def loop():
    while True:
        value[i] = adc.read_adc(0, gain = GAIN)
    print(max(values))
    
    if(max(values)) > sensor_val:
        GPIO.output(PIN, GPIO.LOW)
        print("ON")
        print(PIN)
        time.sleep(0.1)
        
    else:
        GPIO.output(PIN, GPIO.HIGH)
        print("OFF")
        print(PIN)
        time.sleep(0.1)
  
# command to stop operation  
  
def destroy():
    GPIO.output(PIN,GPIO.HIGH)
    GPIO.cleanup()
    
    
    
if __name__ == '__main__':
    setup()
    try:
        loop()
    except KeyboardInterrupt:
        destroy()