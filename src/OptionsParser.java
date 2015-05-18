import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Data is stored in the MacParser object.
 * Parses data from a json file 
 * 
 * @author jkarnuta
 */
public class OptionsParser {

	private final File file;
	private Logger logger;

	/*
	 * Options
	 */
	private String file_type;
	private String itunes_path;
	private String temp_path;
	private String version;
	private String release_date;
	private String author;

	/**
	 * Constructor
	 */
	public OptionsParser(File file){
		this.file = file;
		parse();
	}

	/**
	 * Constructor
	 * Adds a logging object if wanted (no calls will be made unless the logger is specified)
	 * if logger not specified, the errors print to the error stream
	 * @param logger
	 */
	public OptionsParser(File file, Logger logger){
		this(file);
		this.logger = logger;
	}
	
	/**
	 * Saves the current settings into the file
	 * Taken from mkyong.com (fantastic website by the way)
	 */
	public void save(){
		JSONObject mainObj = new JSONObject();
		
		//ytAudioSettings
		JSONObject ytAudioSettingsObj = new JSONObject();
		ytAudioSettingsObj.put("file_type", file_type);
		ytAudioSettingsObj.put("itunes_path", itunes_path);
		ytAudioSettingsObj.put("temp_path", temp_path);
		
		//ProjectMetaData
		JSONObject ProjectMetaDataObj = new JSONObject();
		ProjectMetaDataObj.put("version", version);
		ProjectMetaDataObj.put("release_date", release_date);
		ProjectMetaDataObj.put("author", author);
		
		mainObj.put("ytAudioSettings", ytAudioSettingsObj);
		mainObj.put("ProjectMetaData", ProjectMetaDataObj);
		
		try{
			FileWriter fw = new FileWriter(this.file);
			fw.write(mainObj.toJSONString());
			fw.close();
		} catch(IOException e){
			String message ="Cannot Save JSON: "+e.getMessage();
			if (logger != null) logger.warning(message);
			else System.err.println(message);
		}
	}
	
	/**
	 * Parses the file (tests if it is JSON as well)
	 */
	public void parse(){
		String JSONString = getJSONString();
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(JSONString);
		} catch (ParseException e) {
			String errMessage = "File is NOT JSON: "+e.getMessage();
			if(logger != null) logger.severe(errMessage);
			else System.err.println(errMessage);
			return;
		}

		Map<String, String> ytAudioSettingsMap = (Map<String, String>)jsonObject.get("ytAudioSettings");
		Map<String, String> ProjectMetaDataMap = (Map<String, String>)jsonObject.get("ProjectMetaData");
		//ytAudioSettings
		file_type = ytAudioSettingsMap.get("file_type");
		itunes_path = ytAudioSettingsMap.get("itunes_path");
		temp_path = ytAudioSettingsMap.get("temp_path");
		//ProjectMetaData
		version = ProjectMetaDataMap.get("version");
		release_date = ProjectMetaDataMap.get("release_date");
		author = ProjectMetaDataMap.get("author");
		return;
	}

	/*
	 * A bunch of boring get/set statements for the (currently)
	 * 6 options fields
	 * 
	 * ytAudioSettings:
	 * 1. file_tyle
	 * 2. itunes_path
	 * 3. temp_path
	 * 
	 * ProjectMetaData (No set statements for these):
	 * 4. version
	 * 5. release_date
	 * 6. author
	 */
	//
	public String getFileType(){
		return this.file_type;
	}
	public void setFileType(String file_type){
		this.file_type = file_type;
	}
	//
	public String getItunesPath(){
		return this.itunes_path;
	}
	public void setItunesPath(String itunes_path){
		this.itunes_path = itunes_path;
	}
	//
	public String getTempPath(){
		return this.temp_path;
	}
	public void getTempPath(String temp_path){
		this.temp_path = temp_path;
	}
	
	public String getVersion(){
		return this.version;
	}
	public String getReleaseDate(){
		return this.release_date;
	}
	public String getAuthor(){
		return this.author;
	}


	/**
	 * Returns a string representation of the JSON object within
	 * the file
	 * @return
	 */

	private String getJSONString(){
		StringBuilder sb = new StringBuilder();
		try(BufferedReader br = new BufferedReader(new FileReader(file))){
			String line;
			while ((line = br.readLine())!= null){
				sb.append(line);
			}
		} catch (IOException e) {
			if (logger != null) logger.warning(e.getMessage());
			else System.err.println(e.getMessage());
		}
		return sb.toString();
	}

	public static void main(String [] args){
		OptionsParser op = new OptionsParser(new File("Resources/YTDLSettings.json"));
		System.out.println(op.getFileType());
		op.setFileType(".mp3");
		System.out.println(op.getFileType());
		op.save();
		

	}
}
