import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;


public class BasicParseGobbler extends Thread {

	private Queue<String> buffer;
	private String identifiedString;
	private InputStream is;
	private Logger logger;
	private String identifier;
	private boolean shouldRun;
	
	BasicParseGobbler(InputStream is, Logger logger, String identifier)
	{
		this.is = is;
		//for multithreading
		this.buffer = new ConcurrentLinkedQueue<String>();
		this.logger = logger;
		this.identifier = identifier;
	}
	
	public void commence(){
		shouldRun = true;
		this.start();
	}
	public String getIdentifiedString(){
		return this.identifiedString;
	}
	
	public void terminate(){
		this.shouldRun = false;
		logger.info("<StreamGobbler Alert> Terminated @StreamGobbler (Stream is = "+is.toString()+")");
	}
	public boolean isEmpty(){
		return buffer.isEmpty();
	}

	public String dump(){
		StringBuilder finalString = new StringBuilder();
		while (!buffer.isEmpty())
			finalString.append(buffer.poll()).append("\n");
		return finalString.toString();
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
					if(line.startsWith(identifier)){
						identifiedString = line.substring(identifier.length()).trim();
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
