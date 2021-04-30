import datetime
from flask import Flask, render_template, redirect, url_for, Response
from flask_socketio import SocketIO
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

eventlet.monkey_patch()

from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret'
#async_mode = 'eventlet'
socketio = SocketIO(app)
cpu = CPUTemperature()


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


def uart_read():
    ser = serial.Serial('/dev/HC06')  # open serial port (port, baud rate)
    #print(ser.name)         # check which port was really used
    #ser.write(b'hello')     # write a string
    data = ser.read()
    ser.close()# close port
    return data

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
    bootAWSClient(args.client_id, args.endpoint, args.root_ca, args.key, args.cert)
    socketio.emit('my response',  {'data':'Healthy'})

@socketio.on('server')
def temp_handle():
    port = serial.Serial("/dev/rfcomm0", baudrate=9600, timeout=3.0)
    while True:
        rcv = port.readline()

        time_now = datetime.datetime.now()
        timeString = time_now.strftime("%Y-%m-%d %H:%M:%S")
        #t = str(round(cpu.temperature*1.0))
        t = rcv
        memory = psutil.virtual_memory()
        available = round(memory.available/1024.0/1024.0,1)
        total = round(memory.total/1024.0/1024.0,1)
        info = {
            'time': timeString,
            'temp': t,
            'total': total,
            'available': available,
        }

        socketio.sleep(3)
        #time.sleep(1)
        #publishMessage(info)
        socketio.emit('client',  json.dumps(info))
        socketio.sleep(3)

@app.route('/chart-data')
def chart_data():
    def generate_random_data():
        while True:
            json_data = json.dumps(
                {'time': datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"), 'value': randint(0,10) * 100})
            yield f"data:{json_data}\n\n"
            socketio.sleep(3)

    return Response(generate_random_data(), mimetype='text/event-stream')


if __name__ == "__main__":
   #app.run(host='0.0.0.0', port=80, debug=True)
   #t = Thread(target=temp_handle)
   #t.start()
   socketio.run(app, host='0.0.0.0', port=80, debug=True)