import datetime
from flask import Flask, render_template, redirect, url_for
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient
app = Flask(__name__)

myMQTTClient = AWSIoTMQTTClient("MichaelMoran-RaspberryPi")
myMQTTClient.configureEndpoint("a23h8x757yei1m-ats.iot.us-east-2.amazonaws.com", 8883)

#root CA file, private key file, certificate - relative to your RaspberryPi directory
myMQTTClient.configureCredentials("/home/pi/ee_475/far-future/AWSIoT/AmazonRootCA1.pem", 
    "/home/pi/ee_475/far-future/AWSIoT/private.pem.key", 
    "/home/pi/ee_475/far-future/AWSIoT/certificate.pem.crt")

#connect to AWS IoT core
myMQTTClient.configureOfflinePublishQueueing(10)
myMQTTClient.configureDrainingFrequency(2)
myMQTTClient.configureConnectDisconnectTimeout(10)
myMQTTClient.configureMQTTOperationTimeout(5)

print('Initiating IoT Core Topic ...')
myMQTTClient.connect()

def refreshPage(self, params, packet):
    print('Recieved Message from AWS IoT Core')
    print('Topic: ' + packet.topic)
    print("Payload: ", (packet.payload))
    redirect("/test", param=packet.payload)

@app.route("/")
def plant():
    time_now = datetime.datetime.now()
    timeString = time_now.strftime("%Y-%m-%d %H:%M")
    templateData = {
        #'title' : '',
        'time': timeString
    }
    return render_template('index.html', **templateData)

# @app.route('/test')
# def test(param="Michael is a supid face"):
#     packet = myMQTTClient.subscribe("home/helloworld", 1, refreshPage)
#     return render_template('test.html', packet=param)

if __name__ == "__main__":
   #app.run(host='0.0.0.0', port=80, debug=True)
    app.run()