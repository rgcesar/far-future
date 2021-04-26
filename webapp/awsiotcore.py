import time
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient

def helloworld(self, params, packet):
    print('Recieved Message from AWS IoT Core')
    print('Topic: ' + packet.topic)
    print("Payload: ", (packet.payload))

myMQTTClient = AWSIoTMQTTClient("MichaelMoran-RaspberryPi")
myMQTTClient.configureEndpoint("a23h8x757yei1m-ats.iot.us-east-2.amazonaws.com", 8883)

#root CA file, private key file, certificate - relative to your RaspberryPi directory
myMQTTClient.configureCredentials("/home/pi/ee_475/far-future/AWSIoT/AmazonRootCA1.pem", 
    "/home/pi/ee_475/far-future/AWSIoT/private.pem.key", 
    "/home/pi/ee_475/far-future/AWSIoT/certificate.pem.crt")

#connect to AWS IoT core
myMQTTClient.configureOfflinePublishQueueing(-1)
myMQTTClient.configureDrainingFrequency(2)
myMQTTClient.configureConnectDisconnectTimeout(10)
myMQTTClient.configureMQTTOperationTimeout(5)

print('Initiating IoT Core Topic ...')
myMQTTClient.connect()
# myMQTTClient.subscribe("home/helloworld", 1, helloworld)

# while(True):
#     time.sleep(5)

print("Publishing Message from RPI")
myMQTTClient.publish(
    topic="home/helloworld",
    QoS=1,
    payload="{'Message':'Message by RPI'}"
)