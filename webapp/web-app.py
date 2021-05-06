import datetime
from flask import Flask, render_template, redirect, url_for, Response
from flask_socketio import SocketIO
import numpy as np
from threading import Thread
import serial
import time
from time import sleep
from gpiozero import CPUTemperature
import eventlet
import psutil
import random 
from random import randint
import json
from awsiotcore import bootAWSClient, publishMessage
import argparse
import re
import cv2
import picamera
from water import setup, dispense, soilmoist, lightsensor, fan, light
#import water

eventlet.monkey_patch()

from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret'
#async_mode = 'eventlet'
socketio = SocketIO(app)
cpu = CPUTemperature()
port = serial.Serial("/dev/rfcomm0", baudrate=9600, timeout=3.0)
vc = cv2.VideoCapture(0) 
setup()
socketio.sleep(3)

#global light0 
#global fan0
light0 = 0
fan0 = 0


'''
parser = argparse.ArgumentParser(description="Send and receive messages through and MQTT connection.")
parser.add_argument('--endpoint', required=True, help="Your AWS IoT custom endpoint, not including a port. " +
                                                    "Ex: \"abcd123456wxyz-ats.iot.us-east-1.amazonaws.com\"")
parser.add_argument('--cert', required=True, help="File path to your client certificate, in PEM format.")
parser.add_argument('--key', required=True, help="File path to your private key, in PEM format.")
parser.add_argument('--root-ca', required=True, help="File path to root certificate authority, in PEM format. " +
                                    "Necessary if MQTT server uses a certificate that's not already in " +
                                    "your trust store.")
parser.add_argument('--client-id', default="test-", help="Client ID for MQTT connection.")
parser.add_argument('--topic', default="test/topic", help="Topic to subscribe to, and publish messages to.")
parser.add_argument('--signing-region', default='us-west-2', help="If you specify --use-web-socket, this " +
    "is the region that will be used for computing the Sigv4 signature")

args = parser.parse_args()
'''




def read_bme():
    #socketio.sleep(2)
    port.flushInput()
    rcv = port.readline() # read in bytes
    socketio.sleep(.1)
    rcv = port.readline() # read in bytes
    rcvs = repr(rcv) # turn it into a string
    rcvs2 = [0, 0, 0] # an array with 3 values
    # python regex to extract three floats
    rcvs = re.findall(r"[-+.]?\d*\.\d+|\d+", rcvs) 
    #rcvs[0] = round((float(rcvs[0]) * (5.0/9.0) + 32) , 3) # convert to f
    if len(rcvs) != 3:
        return ['0','0','0']
    print(rcvs)
    #port.close()
    return rcvs

def gen(): 
   while True: 
       rval, frame = vc.read() 
       cv2.imwrite('frame.jpg', frame) 
       yield (b'--frame\r\n' 
              b'Content-Type: image/jpeg\r\n\r\n' + open('frame.jpg', 'rb').read() + b'\r\n') 

@app.route("/")
def plant():
    time_now = datetime.datetime.now()
    timeString = time_now.strftime("%Y-%m-%d %H:%M")
    templateData = {
        #'title' : '',
        'time': timeString
    }
    return render_template('index.html', **templateData)


@socketio.on('connect')
def test_connect():
    #print("client has connected")
    #bootAWSClient(args.client_id, args.endpoint, args.root_ca, args.key, args.cert)

    socketio.emit('my response',  {'data':'Healthy'})

@socketio.on('lighton')
def light_on():
    # toggle the light
    #global light0
    light(1)
    #light0 = ~light0

@socketio.on('lightoff')
def light_off():
    light(0)

@socketio.on('water')
def water_plant():
    # water the plant, two seconds
    dispense(2)

@socketio.on('fanon')
def fan_on():
    # toggle the fan
    #global fan0
    fan(0)
    #fan0 = ~fan0

@socketio.on('fanoff')
def fan_off():
    fan(1)

@socketio.on('server')
def temp_handle():
    #pass
    while True:
        time_now = datetime.datetime.now()
        timeString = time_now.strftime("%Y-%m-%d %H:%M:%S")
        rcvs = read_bme()
        t, press, hum = rcvs
        moist = float(soilmoist())
        light_level = float(lightsensor())
        #moist = 0
        #light_level = 0
        #t = str(round(cpu.temperature*1.0))
        #memory = psutil.virtual_memory()
        #available = round(memory.available/1024.0/1024.0,1)
        #total = round(memory.total/1024.0/1024.0,1)
        info = {
            'time': timeString,
            'temp': str(t),
            'humidity': str(hum),
            'pressure': str(press),
            'soilmoist': str(moist),
            'lightlevel': str(light_level),
        }
        #print(info)
        #socketio.sleep(.1)
        #time.sleep(1)

        #publishMessage(info)

        socketio.emit('client',  json.dumps(info))
        #socketio.sleep(.5)

@app.route('/stream')
def stream():
    # Stream video
    return Response(gen(), mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == "__main__":
   #app.run(host='0.0.0.0', port=80, debug=True)
   #t = Thread(target=temp_handle)
   #t.start()
   socketio.run(app, host='0.0.0.0', port=80, debug=False)
   #print(soilmoist())
