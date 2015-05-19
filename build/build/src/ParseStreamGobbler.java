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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ParseStreamGobbler extends Thread
{
	private InputStream is;
	private boolean shouldRun = true;
	private Queue<String> buffer;
	private int currentProgress;
	private Logger logger;
	private String identifier;
	private StreamType type = StreamType.OUT;
	private static final Pattern PERCENT_PATTERN = Pattern.compile("\\[(.*?)%\\]");
	private static final Pattern ETA_PATTERN = Pattern.compile("ETA:\\ \\[(.*?\\ )s\\]");
	private int oldprogress = Integer.MIN_VALUE;
	private int oldeta = Integer.MAX_VALUE;
	private SpinningState spinningState;

	ParseStreamGobbler(InputStream is, Logger logger, String identifier)
	{
		this.is = is;
		this.shouldRun = true;
		//for multithreading
		this.buffer = new ConcurrentLinkedQueue<String>();
		this.logger = logger;
		this.identifier = identifier;
		this.spinningState = SpinningState.BEFORE;
	}
	
	public SpinningState getSpinState(){
		return spinningState;
	}

	public String getIdentifier(){
		return identifier;
	}

	public StreamType getType(){
		return type;
	}

	public String toString(){
		return buffer.toString();
	}

	public void terminate(){
		this.shouldRun = false;
		logger.info("<StreamGobbler Alert> Terminated @StreamGobbler (Stream "+type+" = "+is.toString()+")");
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

	/**
	 * 
	 * @return progress percent out of 100
	 */
	public int getProgress(){
		return currentProgress;
	}
	
	private String matchedString(String toMatch, Pattern thePattern, int beforeTrim){
		String matched = toMatch.substring(identifier.length()-1).trim();
		Matcher matcher = thePattern.matcher(matched);
		matcher.find();
		matched = matcher.group();
		matched = matched.substring(beforeTrim, matched.length()-2).trim();
		return matched;
	}

	
	private int extractPercent(String in){
		return (int) Double.parseDouble(matchedString(in, PERCENT_PATTERN, 1));
	}
	
	private int extractETA(String in){
		return (int) Double.parseDouble(matchedString(in, ETA_PATTERN, 6));
	}
	/**
	 * Updates the current progress and tells stream if it should add 
	 * line to the buffer
	 * 
	 * @param String from stream
	 * @return if line should be added to the buffer
	 */
	private boolean getUpdate(String in){
		int newProgress = extractPercent(in);
		int newETA = extractETA(in);
		
		//print every 10% or every 5 seconds on ETA as determined by pafy
		boolean shouldAdd = (newProgress >= (oldprogress + 10)) || (oldeta >= newETA + 3);
		if (shouldAdd) {
			//reset variables
			oldprogress = newProgress;
			oldeta = newETA;
		}
		
		currentProgress = newProgress;
		return shouldAdd;
	}

	public void run()
	{
		while(shouldRun){
			try
			{
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line=null;
				//run operations on line
				while ( (line = br.readLine()) != null){
					if(!shouldRun) break;
					if(line.startsWith("beginning extraction") && spinningState == SpinningState.BEFORE){
						spinningState = SpinningState.DOWNLOADING;
						continue;
					}
					else if(line.startsWith("ALTERING") && spinningState == SpinningState.DOWNLOADING){
						spinningState = SpinningState.ALTERING;
						continue;
					}
					else if(line.startsWith("FINISHED") && spinningState == SpinningState.ALTERING){
						spinningState = SpinningState.AFTER;
						continue;
					}
					
					if(line.startsWith(identifier)){
						if (getUpdate(line)){
							buffer.add(line);
						}
					}
					else{
						buffer.add(line);
					}
				}
			}
			catch (IOException ioe)
			{
				logger.severe(ioe.getMessage());  
			}
		}

	}
}
