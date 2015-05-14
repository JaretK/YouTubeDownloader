'''
Created on Apr 11, 2015

@author: jkarnuta
'''
import pafy
from sys import maxint
"""
Used in change_printing_option(total, recvd, ratio, rate, eta)
only print status if eta changes
"""
oldETA = maxint
oldPercent = 0

class youtube_meta_data:
    
    def __init__(self,url_in):
        """
        new Pafy object constructor
        author - uploader of video
        duration - duration of video (MM:SS)
        published - date published (DD Month YYYY)
        title - title of video
        ytid - youtube video id
        """
        self.pafyObject = pafy.new(url_in)
        self.bestAudio = self.pafyObject.getbestaudio()
        
        self.author = self.pafyObject.author
        self.duration = self.convertDuration(self.pafyObject.duration)
        self.published = self.convertDate(self.pafyObject.published)
        self.title = self.pafyObject.title
        self.ytid = self.pafyObject.videoid
        self.bestAudioSize = self.bestAudio.get_filesize()
        self.bitrate = self.bestAudio.bitrate
        
        self.print_option = change_printing_option
    
    def get_pafy_object(self):
        return self.pafyObject
    
    def get_string_meta_data(self):
        """
        returns a string of all the relevant video meta data with headers
        """
        #carriage return
        carrRet = "\n" 
        
        header = "*"*20+carrRet
        title = "TITLE: "+self.title+carrRet
        duration = "DURATION: "+self.duration+carrRet
        author = "AUTHOR: "+self.author+carrRet
        published = "PUBLISHED: "+self.published+carrRet
        ytid = "YOUTUBE ID: "+self.ytid+carrRet
        filesize = "FILE SIZE: "+str(self.bestAudioSize) +" Bytes"+ carrRet
        bitrate = "BITRATE: "+str(self.bitrate)+carrRet
        footer = "*"*20+carrRet
        return unicode(header+title+duration+author+published+ytid+filesize+bitrate+footer).encode("utf-8")
        
    def convertDuration(self, duration_in):
        """
        duration_in format: HH:MM:SS
        convert to: MM:SS
        """
        durationList = duration_in.split(":")
        for i in range(len(durationList)):
            durationList[i] = durationList[i].encode("utf8")
        durationList = [int(x) for x in durationList]
        hours = durationList[0]
        minutes = durationList[1]
        seconds = durationList[2]
        minutes = minutes + hours*60
        # if 4 -> 04, if 11 -> 11
        if minutes / 10 == 0:
            minutes = "0"+str(minutes)
        return unicode(minutes)+u":"+unicode(seconds)
        
    def convertDate(self, date_in):
        """
        date_in format: year-month-day HH:MM:SS
        convert to: date Month year
        ex: 11 April 2015
        """
        newDate = date_in.split(" ")
        #only concerned with the date, the time published is irrelevant
        newDate = newDate[0]
        dateList= newDate.split("-")
        year = int(dateList[0])
        month = int(dateList[1])
        day = int(dateList[2])
        
        intToMonth = {
                      1:"January",
                      2:"February",
                      3:"March",
                      4:"April",
                      5:"May",
                      6:"June",
                      7:"July",
                      8:"August",
                      9:"September",
                      10:"October",
                      11:"November",
                      12:"December"
                      }
        return str(day) + " " + intToMonth[month] + " " + str(year) + " "
        
    
    def download_best_audio(self ,isQuiet = True):
        pafyObject = self.get_pafy_object()
        best_audio = pafyObject.getbestaudio()
        return best_audio.download(quiet = isQuiet, callback = self.print_option)
    
def change_printing_option(total, recvd, ratio, rate, eta):
    global oldETA
    global oldPercent
    
    eta = int(eta)
    percent = int("{0:.0f}".format(ratio*100))

    oldETA = eta
    oldPercent = percent
    design = "[Pafy Download]"
    ratioFormat = "[{0:.1f}%]".format(ratio*100)
    rateFormat = "[{0:.0f} kbps]".format(rate)
    etaFormat = "[{0:.0f} s]".format(eta)
    print design+ " %: " + ratioFormat + " rate: " + rateFormat + " ETA: "+etaFormat
    
if __name__ == "__main__":
    #test URL
    url_in = "https://www.youtube.com/watch?v=hJtNvBtL9rg"
    newYtMetaObject = youtube_meta_data(url_in)
#     print newYtMetaObject.get_string_meta_data()
#     pafyO = newYtMetaObject.get_pafy_object()
#     best_audio = pafyO.getbestaudio()
#     print best_audio.bitrate
#     
#     
#     
#     filename = best_audio.download(quiet=True,r callback = changePrinting)
#     print filename
    filename = newYtMetaObject.download_best_audio()
