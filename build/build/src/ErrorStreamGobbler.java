/**
 * License: GNU GPL
 * This file is part of YouTubeDownloader.

    YouTubeDownloader is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FooYouTubeDownloader is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with YouTubeDownloader.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

class ErrorStreamGobbler extends Thread
{
	InputStream is;
	boolean shouldRun;
	Queue<String> buffer;
	Logger logger;
	StreamType type = StreamType.ERROR;

	ErrorStreamGobbler(InputStream is, Logger logger)
	{
		this.is = is;
		this.shouldRun = true;
		//for multithreading
		this.buffer = new ConcurrentLinkedQueue<String>();
		this.logger = logger;
	}

	public StreamType getType(){
		return type;
	}

	public String toString(){
		return buffer.toString();
	}

	public void terminate(){
		this.shouldRun = false;
		logger.info("<StreamGobbler Alert> Terminated @StreamGobbler (is="+is.toString()+")");
	}

	public String dump(){
		StringBuilder finalString = new StringBuilder();
		while (!buffer.isEmpty())
			finalString.append(buffer.poll()).append("\n");
		return finalString.toString();
	}

	public String peek(){
		return buffer.peek();
	}

	public boolean isEmpty(){
		return buffer.isEmpty();
	}

	public int size(){
		return buffer.size();
	}
	
	public void commence(){
		shouldRun = true;
		this.start();
	}

	public void run()
	{
		while(shouldRun){
			try
			{
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				while ( (line = br.readLine()) != null){
					logger.warning("STDERR: "+line);
					buffer.add(line);
				}
			} catch (IOException ioe)
			{
				logger.severe(ioe.getMessage());  
			}
		}

	}

}