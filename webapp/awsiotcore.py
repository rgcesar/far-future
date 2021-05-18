import datetime
import json
import requests
from AWSIoTPythonSDK.MQTTLib import AWSIoTMQTTClient

tempAgg=0 
humidityAgg=0
pressureAgg=0
soilAgg=0
lightAgg=0
numData=0

tempData = []
humidityData = []
pressureData = []
soilData = []
lightData = []
timeData = []

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
    getHistoricalDataDynamoDB(24)



def storeMessage(sensorData):
    global tempAgg 
    global humidityAgg
    global pressureAgg
    global soilAgg
    global lightAgg
    global numData
    tempAgg = tempAgg + int(sensorData["temp"])
    humidityAgg = humidityAgg + int(sensorData["humidity"])
    pressureAgg = pressureAgg + int(sensorData["pressure"])
    soilAgg = soilAgg + int(sensorData["soilmoist"])
    lightAgg = lightAgg + int(sensorData["light"])
    numData = numData+1

def publishMessage():
    print('Publishing data to iot/1/test')
    global awsMQTTClient
    global tempAgg 
    global humidityAgg
    global pressureAgg
    global soilAgg
    global lightAgg
    global numData
    time_now = datetime.datetime.now()
    time = time_now.strftime("%Y-%m-%d %H")
    sensorData = {
        'timep':str(time),
        'temp': str(tempAgg/numData),
        'humidity':str(soilAgg/numData),
        'pressure': str(pressureAgg/numData),
        'soilmoist': str(soilAgg/numData),
        'lightlevel': str(lightAgg/numData)
    }
    awsMQTTClient.publish(
        topic="iot/1/test",
        QoS=1,
        payload=json.dumps(sensorData)
    )
    tempAgg=0 
    humidityAgg=0
    pressureAgg=0
    soilAgg=0
    lightAgg=0
    numData=0
    

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
    print(json.dumps(response.json(), indent=3))
    return response

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
    else:
        print("Error: invalid call to getHistoricalData: " + sensor)

