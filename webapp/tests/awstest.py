import datetime
from os import times
import time
import json
import random
import argparse
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient
import requests



parser = argparse.ArgumentParser(description="Send and receive messages through and MQTT connection.")
parser.add_argument('--endpoint', required=True, help="Your AWS IoT custom endpoint, not including a port. " +
                                                    "Ex: \"abcd123456wxyz-ats.iot.us-east-1.amazonaws.com\"")
parser.add_argument('--cert', required=True, help="File path to your client certificate, in PEM format.")
parser.add_argument('--key', required=True, help="File path to your private key, in PEM format.")
parser.add_argument('--root-ca', required=True, help="File path to root certificate authority, in PEM format. " +
                                    "Necessary if MQTT server uses a certificate that's not already in " +
                                    "your trust store.")

args = parser.parse_args()


tempData = []
humidityData = []
pressureData = []
soilData = []
lightData = []
timeData = []

global jsonList
jsonList = []

def test():
    bootAWSClient(args.endpoint, args.root_ca, args.key, args.cert)
    batchSendDataDynamoDB(2)
    getHistoricalDataDynamoDB(24)
    print(batchRequestDataDynamoDB("humidity"))
    print()
    print(batchRequestDataDynamoDB("temp"))
    print()
    print(batchRequestDataDynamoDB("pressure"))
    print()
    print(batchRequestDataDynamoDB("lightlevel"))
    print()
    print(batchRequestDataDynamoDB("soilmoist"))

def bootAWSClient(endpoint, root_ca, key, cert):
    global awsMQTTClient
    awsMQTTClient = AWSIoTMQTTClient("test-aggregate-data")
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

def createSampleJSON():
    print("Creating sample JSON message")
    time_now = datetime.datetime.now()
    info = {
        'timep': time_now.strftime("%Y-%m-%d %H:%M"),
        'temp': str(random.randint(50,70)),
        'humidity': str(random.randint(40,60)),
        'pressure': str(random.randint(100,120)),
        'soilmoist': str(random.randint(30,50)),
        'lightlevel': str(random.randint(70,90))
    }
    return info

def storeJsonList(jsonObj):
    global jsonList
    jsonList.insert(0, jsonObj)

def publishJson(time):
    global jsonList
    tempAgg = 0
    humAgg = 0
    pressureAgg = 0
    soilAgg = 0
    lightAgg = 0
    numReadings = len(jsonList)
    for jsonObj in jsonList:
        tempAgg = tempAgg + int(jsonObj['temp'])
        humAgg = humAgg + int(jsonObj['humidity'])
        pressureAgg = pressureAgg + int(jsonObj['pressure'])
        soilAgg = soilAgg + int(jsonObj['soilmoist'])
        lightAgg = lightAgg + int(jsonObj['lightlevel'])
    data = {
        'timep': time,
        'temp': str(float(tempAgg)/numReadings),
        'humidity': str(float(humAgg)/numReadings),
        'pressure': str(float(pressureAgg)/numReadings),
        'soilmoist': str(float(soilAgg)/numReadings),
        'lightlevel': str(float(lightAgg)/numReadings)
    }
    print('Publishing aggregate data to iot/3/test')
    global awsMQTTClient
    awsMQTTClient.publish(
        topic="iot/3/test",
        QoS=1,
        payload=json.dumps(data)
    )
    jsonList = []


def batchSendDataDynamoDB(n):
    time_now = datetime.datetime.now()
    for i in range (0,n):
        for j in range(0,10):
            storeJsonList(createSampleJSON())
        timep = time_now - datetime.timedelta(hours=i)
        publishJson(timep.strftime("%Y-%m-%d %H"))

def requestDataDynamoDB(time):
    URL = "https://tg3po98xd3.execute-api.us-east-2.amazonaws.com/dev/plantdata/"
    # headers
    headers = {"Content-Type":"application/json"}
    # querysting parameter
    params = {"time":time}
    # for Post
    data= {}

    response = requests.request("GET", URL, params=params, headers=headers)
    if response.status_code != 200:
        print("ERROR: Something went wrong with the request. Status Code: " + str(response.status_code))
        print(response.headers)
    else:
        print("API Response Recieved: " + str(response.status_code))
    return response        

def process(response):
    payload = response.json()
    print("humditiy = " + payload["Items"][0]["payload"]["M"]["humidity"]["S"])

def getHistoricalDataDynamoDB(n=24):
    time_now = datetime.datetime.now()
    global tempData
    global humidityData
    global pressureData
    global soilData
    global lightData
    global timeData
    for i in range(0,n):
        time = time_now - datetime.timedelta(hours=i)
        response = requestDataDynamoDB(time.strftime("%Y-%m-%d %H"))
        payload = response.json()
        
        data = payload["Items"][0]["payload"]["M"]
        timeData.insert(0,time.strftime("%Y-%m-%d %H"))
        
        tempData.insert(0, data["temp"]["S"])
        humidityData.insert(0, data["humidity"]["S"])
        pressureData.insert(0, data["pressure"]["S"])
        soilData.insert(0, data["soilmoist"]["S"])
        lightData.insert(0, data["lightlevel"]["S"])

def requestSensorData(sensor, n=24):
    time_now = datetime.datetime.now()
    arr = []
    time_data = []
    for i in range(0,n):
        time = time_now - datetime.timedelta(hours=i)
        response = requestDataDynamoDB(time.strftime("%Y-%m-%d %H"))
        payload = response.json()
        time_data.append(time.strftime("%Y-%m-%d %H"))
        arr.append(payload["Items"][0]["payload"]["M"][sensor]["S"])
    ret = [time_data,arr]
    print(time_data)
    print()
    print(ret[0])

def batchRequestDataDynamoDB(sensor):
    global tempData
    global humidityData
    global pressureData
    global soilData
    global lightData
    global timeData
    if (sensor == "humidity"):
        return [timeData, humidityData]
    elif (sensor == "temp"):
        return [timeData, tempData]
    elif (sensor == "pressure"):
        return [timeData, pressureData]
    elif (sensor == "soilmoist"):
        return [timeData, soilData]
    elif (sensor == "lightlevel"):
        return[timeData, lightData]
    else:
        print("Error: invalid call to getHistoricalData: " + sensor)


if __name__ == '__main__':
    test()