<p align="center">
<img src="https://github.com/rgcesar/projectimages/blob/main/iconfar.png?raw=true" height="250">

</p>

# far-future

<p align="center">
A remotely-controlled automatic plant monitoring and watering system.
</p>



## About

In response to a growing market for more advanced automatic plant waterers and monitors, far-future is the codename for a new plant monitoring system. By using this advanced plant care system, growing healthy plants is now easier than ever!

## Usage

The system requires a few parts:

Main components:
* Raspberry Pi 4
* STM32 Dev Board (e.g., STM32F4DISCOVERY)

Sensors/Peripherals compatible with RPi4:
* HC-06 Bluetooth Module
* BME280 Sensor
* Water Level Sensor
* Fan
* Photoresistor
* Camera Module
* Water pump(s) with supporting hardware (relays, batteries, hoses, etc.)

...and compatible plants suited for the indoors

### Step 1: Flash the STM32 board with sensor_control.zip

The STM32cube IDE is recommended to do this.

### Step 2: Connect system components

Wiring diagram for STM32
<p align="center">
<img src="https://github.com/rgcesar/projectimages/blob/main/stm32wire.jpg?raw=true" width="300">

</p>

Wiring diagram for RPi4
<p align="center">
<img src="https://github.com/rgcesar/projectimages/blob/main/rpiwire.jpg?raw=true" width="300">


</p>

### Step 3: Attach sensors and water hoses to plant
Example
<p align="center">
<img src="https://github.com/rgcesar/projectimages/blob/main/plant2.jpg?raw=true" width="300">

</p>

### Step 4: Run script
```
$ python3 ./webapp/web-app.py
```

After running the script, the webpage will be hosted on the RPi4. 

### Demo

<p align="center">
<img src="https://github.com/rgcesar/projectimages/blob/main/webpage.gif?raw=true" width="700" />
</p>

### Project Schedule

<p align="center">
<img src="https://github.com/rgcesar/projectimages/blob/main/developmentsch.png?raw=true" height="600" />
</p>

### Diagrams
<p align="center">
<img src="https://github.com/rgcesar/projectimages/blob/main/design1.png?raw=true" width="500" />
<img src="https://github.com/rgcesar/projectimages/blob/main/sequencechart.png?raw=true" width="500" />
<img src="https://github.com/rgcesar/projectimages/blob/main/finaldesign.png?raw=true" width="500" />
</p>
