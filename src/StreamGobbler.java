import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.logging.Logger;

class StreamGobbler extends Thread
	{
		InputStream is;
		StreamType type;
		boolean shouldRun;
		volatile Queue<String> buffer;
		Logger logger;
		String identifier;

		StreamGobbler(InputStream is, StreamType type, Logger logger, Queue<String> synchronizedQueue)
		{
			this.is = is;
			this.type = type;
			this.shouldRun = true;
			//for multithreading
			this.buffer = synchronizedQueue;
			this.logger = logger;
			if (type == StreamType.ERR) this.identifier = "ERR";
			else this.identifier = "OUT";
		}
		
		public String getIdentifier(){
			return identifier;
		}

		public synchronized String check(){
			return buffer.peek();
		}

		public void terminate(){
			this.shouldRun = false;
			logger.info("<StreamGobbler Alert> Terminated @StreamGobbler (is="+is.toString()+")");
		}
		
		public synchronized String dump(){
			StringBuilder finalString = new StringBuilder();
			while (!buffer.isEmpty())
				finalString.append(buffer.poll()).append("\n");
			finalString.append("\n");
			return finalString.toString();
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
						synchronizedAdd(line);
					}
				} catch (IOException ioe)
				{
					logger.severe(ioe.getMessage());  
				}
			}

		}
		
		private synchronized void synchronizedAdd(String toAdd){
			buffer.add(toAdd);
		}
	}