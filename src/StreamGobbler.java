import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

import javafx.scene.control.TextArea;

class StreamGobbler extends Thread
	{
		InputStream is;
		StreamType type;
		boolean shouldRun;
		Queue<String> buffer;
		Logger logger;
		TextArea toPush;
		String identifier;

		StreamGobbler(InputStream is, StreamType type, Logger logger, TextArea toPush)
		{
			this.is = is;
			this.type = type;
			this.shouldRun = true;
			this.buffer = new LinkedList<String>();
			this.logger = logger;
			this.toPush = toPush;
			if (type == StreamType.ERR) this.identifier = "ERR";
			else this.identifier = "OUT";
		}
		
		public String getIdentifier(){
			return identifier;
		}

		public String toString(){
			return buffer.peek();
		}

		public void terminate(){
			this.shouldRun = false;
			logger.info("<StreamGobbler Alert> Terminated @StreamGobbler (is="+is.toString()+")");
		}
		
		public String dump(){
			return buffer.poll();
		}
		
		public boolean isEmpty(){
			return buffer.isEmpty();
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
						System.out.println(line);
						buffer.add(line);
					}
				} catch (IOException ioe)
				{
					logger.severe(ioe.getMessage());  
				}
			}

		}
	}