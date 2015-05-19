Installation Instructions:

There are three steps to install on a mac (currently there is no build for windows or linux systems because I cannot test the deployment on those machines. Let me know if you can help with that!)

1. Navigate to your /usr/bin using terminal via the command "open /usr/bin" (no quotes). Then copy and paste the ffmpeg and ffprobe binaries into the directory (this will require an admin password). This puts these binaries into your PYTHONPATH (so if you know how your path is set up, you can put these binaries elsewhere in your path)

2. download the dmg and click it to open the installation process. The app doesn't have to be in your Applications folder as it is a standalone distributions. The app requires python 2.x to be the default on your system. To check this, type these commands in your terminal in sequential order (that is, press enter between them)

i. python
ii. import sys
iii. print sys.version

if you get an error from the print sys.version line, try typing print(sys.version). If you are running on a vanilla mac, you probably have a 2.x python interpreter as your default one. If you had the inclination to change it to a 3.x version, you can probably change it back (or fork my code at github.com/JaretK and build your own distribution with 2to3 ran on the python files). To exit this python session, type sys.exit(0)

3. Open the application. You'll have to click preferences -> update and set the iTunes file path to your automatically add to itunes folder (I tried to guess the location based on a standard mac file system). Click the "find" button to the right of the text field to help your find the file path. It *should* be in /users/YOURUSERNAME/music/itunes/automatically add to itunes.localized, but my file system is different and yours might be as well. You can leave the temp filepath option alone. You only need to do this once since the program remembers your choices.

Thats it! Remember you can always validate your song + artist selections by clicking tools -> validate URL to listen to the song first