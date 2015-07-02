import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.logging.Logger;


public class ExtractToFile {

	String filePath;
	private final String finalName;
	private final InputStream is;
	private final String tempFilePath = Constants.tempFilePath;
	private final Logger logger;

	public ExtractToFile(String resourceName, String finalName, Logger logger){
		this.finalName = finalName;
		this.logger = logger;
		this.is = this.getClass().getResourceAsStream(resourceName);
	}

	private String makeFile() throws IOException{
		String finalPath = tempFilePath+finalName;
		OutputStream resStreamOut = null;
		try {
			int readBytes;
			byte[] buffer = new byte[4096];
			File outputFile = new File(finalPath);
			resStreamOut = new FileOutputStream(outputFile);
			while ((readBytes = is.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (Exception ex) {
			throw ex;
		} finally {
			is.close();
			resStreamOut.close();
		}
		return finalPath;
	}

	public boolean fileExists(){
		File testExistence = new File(tempFilePath+finalName);
		return testExistence.exists();
	}

	public String getFilePath(){
		/*
		 * If file exists, delete it for overwrite and make new file
		 * else, make new file
		 */
		String filePath = "";
		try {
			if(!fileExists())
				filePath = makeFile();
			else
				new File(tempFilePath+finalName).delete();
				filePath = makeFile();
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
		return filePath;
	}

	public String forceNewFile(){
		String filePath = "";
		try {
			filePath = makeFile();
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
		return filePath;
	}
}
