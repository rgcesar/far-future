import datetime
from flask import Flask, render_template, redirect, url_for, Response
from flask_socketio import SocketIO
from threading import Thread
import serial
from time import sleep
from gpiozero import CPUTemperature
import eventlet
from random import randint
import json
from awsiotcore import bootAWSClient, storeMessage, publishMessage, batchRequestDataDynamoDB, getHistoricalDataDynamoDB
import argparse
import re
import cv2
from water import setup, dispense, soilmoist, lightsensor, fan, light
from get_stream import get_stream
from write_stream import write_stream

eventlet.monkey_patch()

from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret'
#async_mode = 'eventlet'
socketio = SocketIO(app)
#cpu = CPUTemperature()
port = serial.Serial("/dev/rfcomm0", baudrate=9600, timeout=3.0)
vc = cv2.VideoCapture(0) 
setup()
socketio.sleep(3)

#global light0 
#global fan0
light0 = 0
fan0 = 0
userctl = 0 # variable to select automation
values = {
    'soils': 100,
    'lights': 100,
    'temps': 100
}



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






def read_bme():
    port.flushInput()
    socketio.sleep(0)
    rcv = port.readline() # read in bytes
    socketio.sleep(.1)
    rcv = port.readline() # read in bytes
    rcvs = repr(rcv) # turn it into a string
    # python regex to extract three floats
    rcvs = re.findall(r"[-+.]?\d*\.\d+|\d+", rcvs)
   
    if len(rcvs) != 3:
        return ['0','0','0']
    #print(rcvs)
    return rcvs


def gen(): 
   while True: 
       socketio.sleep(0)
       rval, frame = vc.read() 
       cv2.imwrite('frame.jpg', frame) 
       socketio.sleep(0)
       yield (b'--frame\r\n' 
              b'Content-Type: image/jpeg\r\n\r\n' + open('frame.jpg', 'rb').read() + b'\r\n') 


'''
def gen():
    video_getter = get_stream(0).start()
    video_shower = write_stream(video_getter.frame).start()

    while True:
        if video_getter.stopped or video_shower.stopped:
            video_shower.stop()
            video_getter.stop()
            break

        frame = video_getter.frame
       
        cv2.imwrite('frame.jpg', frame)
        #video_shower.frame = frame

        yield (b'--frame\r\n' 
              b'Content-Type: image/jpeg\r\n\r\n' + open('frame.jpg', 'rb').read() + b'\r\n')
'''

@app.route("/")
def plant():
    time_now = datetime.datetime.now()
    timeString = time_now.strftime("%Y-%m-%d %H:%M")
    templateData = {
        'time': timeString
    }
    return render_template('index.html', **templateData)



@socketio.on('connect')
def test_connect():
    print("client has connected")
    bootAWSClient(args.client_id, args.endpoint, args.root_ca, args.key, args.cert)
 
    socketio.emit('update value', {'soils':values['soils']}, broadcast=True)
    socketio.emit('update value', {'lights':values['lights']})
    socketio.emit('update value', {'temps':values['temps']})
    #socketio.emit('update value', message, broadcast=True)

    socketio.emit('my response',  {'data':'Healthy'})

@socketio.on('lighton')
def light_on(ctl=1):
    # toggle the light
    global userctl
    userctl = ctl
    light(1)
    
@socketio.on('lightoff')
def light_off(ctl=1):
    global userctl
    userctl = ctl
    light(0)

@socketio.on('water')
def water_plant():
    # water the plant, two seconds
    dispense(2)

@socketio.on('fanon')
def fan_on(ctl=1):
    # toggle the fan
    #global fan0
    global userctl
    userctl = ctl
    fan(0)
    #fan0 = ~fan0

@socketio.on('fanoff')
def fan_off(ctl=1):
    global userctl
    userctl = ctl
    fan(1)

@socketio.on('Slider value changed')
def value_changed(message):
    global userctl
    userctl = 0
    socketio.emit('overrideoff',  '')
    values[message['who']] = message['data']
    socketio.emit('update value', message, broadcast=True)

@socketio.on('chart')
def get_chart(message):
    #print("message = " + message)
    sdata = batchRequestDataDynamoDB(message)
    #print(sdata)
    socketio.emit('chart_data',  json.dumps(sdata))

@socketio.on('server')
def temp_handle():
    global userctl
    while True:
        time_now = datetime.datetime.now()
        timeString = time_now.strftime("%Y-%m-%d %H:%M:%S")
        socketio.sleep(0)
        rcvs = read_bme()
        t, press, hum = rcvs
        moist = soilmoist()
        socketio.sleep(0)
        light_level = lightsensor()
        socketio.sleep(0)
        info = {
            "time": timeString,
            "temp": str(t),
            "humidity": str(hum),
            "pressure": str(press),
            "soilmoist": str("{:.1f}".format(moist)),
            "lightlevel": str("{:.1f}".format(light_level)),
        }
        if userctl == 0:
            socketio.emit('overrideoff',  '')
            if int(float(light_level)) < int(values['lights']) :
                light_on(0)
            else:
                light_off(0)
            if int(float(t)) > int(values['temps']):
                fan_on(0)
            else: fan_off(0)
            if int(float(moist)) < int(values['soils']):
                water_plant()
        else:
            socketio.emit('overrideon',  '')
        
        storeMessage(info)
        if time_now.minute == 0:
           publishMessage()
           getHistoricalDataDynamoDB()
        socketio.emit('client',  json.dumps(info))
        
@app.route('/stream')
def stream():
    # Stream video
    socketio.sleep(0)
    return Response(gen(), mimetype='multipart/x-mixed-replace; boundary=frame')


if __name__ == "__main__":
   #t = Thread(target=temp_handle)
   #t.start()
   socketio.run(app, host='0.0.0.0', port=80, debug=False)
   
