import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Logger;


public class ExtractResource {

	private String resourceAsString;
	private Logger logger;
	private InputStream resourceInputStream;
	
	public ExtractResource(String resourceName, Logger logger){
		this.resourceInputStream = this.getClass().getResourceAsStream(resourceName);
		this.logger = logger;
		readResource();
	}
	private void readResource(){
		StringBuilder lineBuilder = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new InputStreamReader(this.resourceInputStream))) {
			String line;
			while((line = br.readLine())!= null){
				lineBuilder.append(line);
			}
		} catch (IOException e) {
			this.logger.warning(e.getMessage());
		}
		this.resourceAsString = lineBuilder.toString();
	}
	public String toString(){
		return this.resourceAsString;
	}
}
