import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

class StreamGobbler extends Thread
	{
		InputStream is;
		StreamType type;
		boolean shouldRun;
		Queue<String> buffer;
		Logger logger;
		
		StreamGobbler(InputStream is, Logger logger)
		{
			this.is = is;
			this.shouldRun = true;
			//for multithreading
			this.buffer = new ConcurrentLinkedQueue<String>();
			this.logger = logger;
		}

		public String toString(){
			return buffer.toString();
		}

		public void terminate(){
			this.shouldRun = false;
			System.out.println();
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

		public void run()
		{
			while(shouldRun){
				try
				{
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					String line=null;
					while ( (line = br.readLine()) != null){
						buffer.add(line);
					}
				} catch (IOException ioe)
				{
					logger.severe(ioe.getMessage());  
				}
			}

		}

	}