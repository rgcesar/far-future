import time
import json
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient


def bootAWSClient(client_id, endpoint, root_ca, key, cert):
    global awsMQTTClient
    awsMQTTClient = AWSIoTMQTTClient(client_id)
    awsMQTTClient.configureEndpoint(endpoint, 8883)

    #root CA file, private key file, certificate - relative to your RaspberryPi directory
    awsMQTTClient.configureCredentials(root_ca, key, cert)

    #connect to AWS IoT core
    awsMQTTClient.configureOfflinePublishQueueing(-1)
    awsMQTTClient.configureDrainingFrequency(2)
    awsMQTTClient.configureConnectDisconnectTimeout(10)
    awsMQTTClient.configureMQTTOperationTimeout(5)

    print('Initiating IoT Core Topic ...')
    awsMQTTClient.connect()

def publishMessage(data):
    print('Publishing data to iot/1/test')
    global awsMQTTClient
    awsMQTTClient.publish(
        topic="iot/1/test",
        QoS=1,
        payload=json.dumps(data)
    )