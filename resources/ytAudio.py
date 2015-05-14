#!/usr/bin/env python
# -*- coding: utf-8 -*- 
'''
Created on Jan 2, 2015

@author: jkarnuta

LICENSE: GNU GPL. See Licenses/COPYING for more

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

'''
#required custom imports:
#mutagen
#pafy

#imports for changing file
import glob
from mutagen.easyid3 import EasyID3
from mutagen.mp3 import MP3
import os
import shutil
  
#imports for getURL
import re
import urllib2
import BeautifulSoup
 
#imports for get_meta_data AND getAudio
import youtube_meta_data
  
#imports used throughout
import time
import sys
import subprocess

#global variables for ytAudio module
number_videos=0
song_info = ""
yt_id = ""
yt_duration = 0
song_meta = []
ytMetaData = None
downloaded_filename = ""

#options
file_type = ".mp3"
itunes_filepath = "/Volumes/Macintosh HD/Media/iTunes Library/Automatically Add to iTunes.localized"
temp_filepath = "/tmp/ytAudio_temp"
#options to add:
#file_tyle, currently converts m4a to mp3, but m4a is arguably better. Need a new tagging system, TagID or similar

"""
general ytAudio workflow:

-ytAudio_main (requires command line arguments)
-MakeTempLocation
-get_yt_url
-getAudio
    -download video with youtube_meta_data and PAFY
-alter_file (checks if file already exists in itunes via check_existence(file_name) )
-rm temp location
"""

#---- Main check
def ytAudio_main():
    global song_meta
    global file_type
    global itunes_filepath
    global temp_filepath
    global yt_id
    """
    Loads the settings from the project_settings.ini file 
    """
#     load_settings()
    """
    re-orient program to temporary location and delete contents of temp folder
    """
    makeTempLocation()
    before = getTime()
    """
    gets user input
    """
    #sys.argv -> ["script name", "song", "artist", "options", 
    #    file_type, itunes_location, tmp_location]
    indexes = [0,1,2]
    song_meta = [sys.argv[i+1] for i in indexes]
    file_type = sys.argv[4]
    itunes_filepath = sys.argv[5]
    temp_filepath = sys.argv[6]
        
    #song_meta = ["song", "artist","options"]
    
    """
    if file is already downloaded, don't download and remove temp tree
    """
    file_artist = make_cap(song_meta[0])
    file_title = make_cap(song_meta[1])
    if check_existence(file_artist, file_title):
        shutil.rmtree(temp_filepath)
        raise SystemExit(1)
    
    """
    gets youtube url from user input
    """
    yt_url = get_yt_URL()
    
    """
    makes ytMetaData object, instance of youtube_meta_data
    """
    create_meta_data(yt_url)
    """
    assigns yt_id to ytMetaData.ytid
    """
    yt_id = ytMetaData.ytid

    """
    downloads audio, assigns downloaded_filename
    """
    getAudio(yt_url)
    """
    converts file, moves to itunes
    """
    print "ALTERING"
    alter_file()
    """
    deletes the temp_filepath from file system
    """
    shutil.rmtree(temp_filepath)
    print "FINISHED"
    print "\nTotal process finished in "+diff_time(before)+"s"
    return
    

def makeTempLocation():
    #location and create temp folder
    if not os.path.exists(temp_filepath):
        os.mkdir(temp_filepath)
    #re-orient to temp folder    
    os.chdir(temp_filepath)
    
    #delete contents of temp folder
    files = glob.glob(temp_filepath+"/*")
    for f in files:
        os.remove(f)
    return

#prompts user for input, retrieves URL address for song name 
def get_yt_URL():
    #Define Fields and Constants
    global number_videos
    global song_info
    global yt_id
    global song_meta
    global yt_duration
    
    youtube_url_search = "https://www.youtube.com/results?search_query="
    youtube_results_id = "section-list"
    begin_time = time.time()
    song_meta = [x.strip() for x in song_meta]
    song_info = song_meta[0]+" "+song_meta[1]

    #remove text within brackets --> [TEXT] 
    song_meta[0] = re.sub("\[.*?\]","",song_meta[0])
    song_meta[1] = re.sub("\[.*?\]","",song_meta[1])
    
    if song_meta[2] != "":
        if "ytid" in song_meta[2]:
            id_list = [x.strip() for x in song_meta[2].split("=")]
            yt_id = id_list[1]
            yt_duration = "-:--"
            return "https://www.youtube.com/watch?v="+yt_id  
        else:
            print "ytid not in third argument..."
            print "Exiting ytAudio"
            sys.exit(1)
            
    #---------------- System Exiting Block
    if not song_info:
        print("no song entered, exiting")
        raise SystemExit(0) # 0 == exited without error 
    #----------------
    
    print "fetching youtube video IDs..."
    
    #get webpage HTML
    #convert spaces to url %20 
    urlToGet = re.sub(" ", "%20", youtube_url_search+song_info)
    getHTML = urllib2.urlopen(urlToGet).read()
    
    #yt_id is the youtube video id of the most relevant video
    #yt_duration is the length of the most relevant video
    #resultHTML = HTML tag from id = youtube_results_id
    soup = BeautifulSoup.BeautifulSoup(getHTML)

    yt_tuple = renderHTML(str(soup.find("ol",{"class":youtube_results_id}))) #(yt_id, yt_duration)
    yt_id = yt_tuple[0]
    yt_duration = yt_tuple[1]

    print str(number_videos) +" youtube urls fetched in "+diff_time(begin_time)+"s"
    return "https://www.youtube.com/watch?v="+yt_id
    
def getAudio(yt_url):
    #fields and constants
    global song_info
    global yt_id
    global yt_duration
    global ytMetaData
    global downloaded_filename
    
    print "Video information:\n"
    print ytMetaData.get_string_meta_data()
    
    #call youtube_dl to download youtube video
    print "beginning extraction..."
    begin_time = getTime()
    #downloads audio through youtube_meta_data
    
    downloaded_filename = ytMetaData.download_best_audio()
        
    print "Downloaded ["+ytMetaData.title +"] in "+diff_time(begin_time)+"s"

    return

def create_meta_data(url_in):
    global ytMetaData
    ytMetaData = youtube_meta_data.youtube_meta_data(url_in)
    return

#changes file's ID3 meta data to include song information. Converts aac to mp3
def alter_file():

    #define fields and constants
    global song_meta
    global test_filepath
    global itunes_filepath
    global downloaded_filename
    global file_type
    current_directory = os.getcwd()
    
    m4a_file = downloaded_filename
    audio_file_artist = make_cap(song_meta[1])
    audio_file_title = make_cap(song_meta[0])
    begin_time = getTime()
    
#     if file_type == ".mp3": #FILE_TYPE not functional yet
    #get aac filename and convert to mp3 using ffmpeg through bash 
    #deletes aac file and prints statistics
    print "converting..."
    mp3_file = m4a_file[:-4]+".mp3"
    
    subprocess.call([
            "ffmpeg","-loglevel", "quiet" ,"-i",
            os.path.join(current_directory, m4a_file),
            "-acodec", "libmp3lame", "-ab", "256k",
            os.path.join(current_directory, mp3_file)
            ])
    os.remove(current_directory+"/"+m4a_file)
    
    print "converted m4a -> mp3 in "+diff_time(begin_time)+"s"
    
    begin_time = getTime()
    #adds ID3 tags to mp3_file
    audio_file = MP3(mp3_file, ID3 = EasyID3)
    audio_file["artist"] = unicode(audio_file_artist)
    audio_file["title"] = unicode(audio_file_title)
    audio_file.save()
     
    #move file from python directory to automatically add to itunes 
    #automatically add to itunes: itunes_filepath
    current_location = current_directory+"/"+mp3_file 
   
    try:
        shutil.move(current_location, itunes_filepath)
    except:
        print "\nError: "+mp3_file+" already exists in " +itunes_filepath
        raise SystemExit(1)
    
  
    print ""
    print "*"*20
    print "ARTIST: "+audio_file_artist
    print "SONG NAME: "+audio_file_title+file_type
    print "DESTINATION: "+itunes_filepath
    print "Moved successfully in "+diff_time(begin_time)+"s"
    print "*"*20
    print ""
    
    return
    
#returns the first occurrence of the text located between sand and wich
def renderHTML(inner_HTML):
    #use regex for sandwich
    #first part of tuple. Return youtube_id of first video
    #result contained in video_list[0]
    global number_videos
    sand = "data-context-item-id=\""
    wich = "\""
    video_list = re.findall(sand+"(.*?)"+wich, inner_HTML)
    number_videos = len(video_list)
    
    sand = "Duration:"
    wich = "\.<"
    duration_list = re.findall(sand+"(.*?)"+wich, inner_HTML)
    return (video_list[0],duration_list[0].strip())
"""
gets current system time
"""
def getTime():
    return time.time()
"""
returns the formatted difference between current time and old_time
format: 0.0000s
"""
def diff_time(old_time):
    diff = getTime() - old_time
    return "{0:.4f}".format((diff*100)/100)
"""
Intelligent capitalization function
if # spaces > 2*capital letters, _
    make all letters after a non-letter _
    (letter includes alphabet and ') capitalized
"""
def make_cap(string_in):
    # if number of spaces is at least twice as high as the number of capital letters
    if(string_in.count(" ") >= 2*len(re.findall("[A-Z]", string_in))):
        string_list = string_in.split(" ")
        cap_re = re.compile("\w+")
        return ' '.join([cap_re.sub(lambda y: y.group(0).capitalize(), x, 1) for x in string_list]).strip()
    return string_in

"""
checks to see if file already exists in itunes
"""
def check_existence(file_artist, file_title):
    filepath = itunes_filepath
    pathComponents = filepath.split("/") 
    pathComponents = pathComponents[:-1]
    pathComponents.append("Music")
    pathComponents.append(file_artist)
    pathComponents.append("Unknown Album")
    newFilePath = "/".join(pathComponents) + "/"+file_title+file_type
    
    inITunes = os.path.isfile(newFilePath)
    if inITunes:
        print >> sys.stderr, "ERROR: "+file_title+" by: "+file_artist+ " already exists in iTunes"
        return True
    
    tempFiles = os.listdir(itunes_filepath)
    for item in tempFiles:
        if file_title in item:
            print >> sys.stderr, "ERROR: "+file_title+" by: "+file_artist+ " already exists in: "+itunes_filepath
            return True
    
    return inITunes

if __name__ == "__main__":
    #prompts user for song name
    #song_meta[0] = artist
    #song_meta[1] = title
    ytAudio_main()
    