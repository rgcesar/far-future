from flask import Flask, render_template, Response
from flask_socketio import SocketIO
from threading import Thread
import datetime
import serial
import time
from time import sleep
from gpiozero import CPUTemperature
import eventlet
import psutil
import random 
from random import randint
import json

eventlet.monkey_patch()
app = Flask(__name__)
app.config['SECRET_KEY'] = 'secret'
#async_mode = 'eventlet'
socketio = SocketIO(app)
cpu = CPUTemperature()

def uart_read():
    ser = serial.Serial('/dev/ttySO')  # open serial port (port, baud rate)
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
    socketio.emit('my response',  {'data':'Healthy'})

@socketio.on('server')
def temp_handle():
    while True:
        time_now = datetime.datetime.now()
        timeString = time_now.strftime("%Y-%m-%d %H:%M:%S")
        t = str(round(cpu.temperature*1.0))
        memory = psutil.virtual_memory()
        available = round(memory.available/1024.0/1024.0,1)
        total = round(memory.total/1024.0/1024.0,1)
        info = {
            'time': timeString,
            'temp': t,
            'total': total,
            'available': available,
        }

        socketio.sleep(.5)
        #time.sleep(1)
        
        socketio.emit('client',  json.dumps(info))
        socketio.sleep(0)

@app.route('/chart-data')
def chart_data():
    def generate_random_data():
        while True:
            json_data = json.dumps(
                {'time': datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S"), 'value': randint(0,10) * 100})
            yield f"data:{json_data}\n\n"
            socketio.sleep(0)

    return Response(generate_random_data(), mimetype='text/event-stream')




if __name__ == "__main__":
   #app.run(host='0.0.0.0', port=80, debug=True)
   #t = Thread(target=temp_handle)
   #t.start()
   socketio.run(app, host='0.0.0.0', port=80, debug=True)
