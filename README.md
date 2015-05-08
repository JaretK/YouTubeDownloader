# YouTubeDownloader
Custom gui and wrapper for pafy that downloads audio from youtube videos

<ul>
<li>Front end written in JavaFX (using <u>e(fx)clipse</u> as the javaFX runtime and tooling environment), back end written in python using pafy as the downloader.</li>

<li>Settings file written using an XML file format.</li> 

<li>File comes with ffmpeg and ffprobe tools ready to be written to the user's <code>/usr/bin</code> directory.</li>

</ul> 

Program directory:

<ul>
JavaFX front end:
<ul>
<li>MainViewer.java</li>

</ul>
Python back end:
<ul>
<li>ytAudio: provides the driver for downloading the audio file</li>
<li>ytAudioSettings: provides the backbone for parsing the settings file. Possibly deprecated (might move to java)</li>
<li>youtube_meta_data: incharge of actually downloading the audio file and returns the file name of the downloaded audio file</li>

</ul>
</ul>
