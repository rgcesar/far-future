from threading import Thread
import cv2

class write_stream:

    def __init__(self, frame=None):
        self.frame = frame
        self.stopped = False

    def start(self):
        Thread(target=self.show, args=()).start()
        return self

    def show(self):
        while not self.stopped:
            key = 0
            #cv2.imshow("Video", self.frame)
            #cv2.imwrite('frame.jpg', self.frame) 
            if cv2.waitKey(1) == ord("q"):
               self.stopped = True

    def stop(self):
        self.stopped = True