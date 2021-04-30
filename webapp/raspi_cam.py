import subprocess

def takePicture():
    subprocess.call("./scripts/raspistill.sh")

takePicture()
