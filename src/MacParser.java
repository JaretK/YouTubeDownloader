import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Class designed specifically to parse the settings information stored in a
 * .mac file. The .mac file extension is purely for satire and does not follow
 * and previously conceived style definitions (although it is inspired by xml and
 * simplified from that format). The files are laid out in this manner:
 * 
 * <SettingGroup>
 * key1 = value1
 * key2 = value2
 * key3 = value3
 * ...
 * </SettingGroup>
 * 
 * As many SettingClasses as necessary may be used, but they should be escaped.
 * 
 * @author jkarnuta
 *
 */
public class MacParser {

	final File file;
	private Logger logger;
	/*
	 * mapOfMaps maps the SettingGroup to the HashMap containing the Key : Value pairs 
	 * for the options inside the map
	 */
	private HashMap<String, HashMap<String, String>> mapOfMaps;

	/**
	 * Constructor
	 */
	public MacParser(File file){
		this.file = file;
		this.logger = null;
		this.mapOfMaps = new HashMap<String,HashMap<String, String>>();
		parse();
	}

	/**
	 * Constructor
	 * Adds a logging object if wanted (no calls will be made unless the logger is specified)
	 * if logger not specified, the errors print to the error stream
	 * @param logger
	 */
	public MacParser(File file, Logger logger){
		this.file = file;
		this.logger = logger;
		this.mapOfMaps = new HashMap<String,HashMap<String, String>>();
		parse();
	}

	/**
	 * Returns the SettingGroup fields of the map
	 * @return the SettingGroup fields
	 */
	public Set<String> getSettingsGroups(){
		return mapOfMaps.keySet();
	}

	/**
	 * Gets the options (keys) of the specified SettingGroup
	 * @param SettingGroup to get optionss from
	 * @return Set of options
	 */
	public Set<String> getOptions(String SettingGroup){
		if(!mapOfMaps.keySet().contains(SettingGroup)) return new HashSet<String>();
		return mapOfMaps.get(SettingGroup).keySet();
	}

	/**
	 * Gets the value associated with the specified option inside the SettingGroup
	 * @param SettingGroup
	 * @param Key = Option
	 * @return value
	 */
	public String getValue(String SettingGroup, String Key){
		return mapOfMaps.get(SettingGroup).get(Key);
	}

	/**
	 * Sets the value of the Key inside the specified SettingGroup
	 * @param SettingGroup
	 * @param Key
	 * @param Value
	 */
	public void setValue(String SettingGroup, String Key, String Value){
		mapOfMaps.get(SettingGroup).put(Key, Value);
	}

	public static void main(String[] args){
		MacParser mp = new MacParser(new File("Resources/YTDLSettings.mac"));
		System.out.println(mp.getSettingsGroups());
		System.out.println(mp.getOptions("ytAudioSettings"));
		System.out.println(mp.getValue("ytAudioSettings", "itunes_path"));
	}
	
	/**
	 * Extracts the options from the settings file
	 */
	private void parse(){
		String line;
		String regex = "<(.*?)>";
		String currentSettingGroup = null;
		HashMap<String, String> currentSettingGroupMap = new HashMap<String, String>();
		boolean currentSettingGroupOpen = false; //currently have a SettingsGroup open (parsing options)

		try (BufferedReader br = new BufferedReader(new FileReader(file)))
		{
			while ((line = br.readLine()) != null)
			{
				//found a start/end of settingsgroup
				if(line.matches(regex)){
					//beginning of SettingGroup
					if (!currentSettingGroupOpen){
						currentSettingGroup = line.substring(1, line.length()-1);
						currentSettingGroupMap = new HashMap<String, String>();
						mapOfMaps.put(currentSettingGroup, currentSettingGroupMap);
						currentSettingGroupOpen = true;
						continue;
					}
					else if (currentSettingGroup.equals(line.substring(2, line.length()-1))
							&& currentSettingGroupOpen){
						currentSettingGroup = null;
						currentSettingGroupMap = null;
						currentSettingGroupOpen = false;
						continue;
					}
					else{
						if(logger != null){
							logger.warning("Line matched regex (<>) in an invalid manner");
							logger.warning("Line: "+line+ " Is Invalid");}
						else{
							System.err.println("Line matched regex (<>) in an invalid manner");
							System.err.println("Line: "+line+ " Is Invalid");
						}

						continue;
					}
				}
				if(currentSettingGroupOpen){
					//line did not match <>
					//must have an open SettingGroup though
					String [] items = line.split("=");
					for (int i = 0; i < items.length; i++) items [i] = items[i].trim();
					currentSettingGroupMap.put(items[0], items[1]);
				}
			}
		} catch (IOException e){
			if(logger != null)
				logger.severe(e.getMessage());
			else
				e.printStackTrace();
		}
	}

	/**
	 * General Parsing Algorithm (assuming linear iteration)
	 * 
	 * currentSettingGroup = null
	 * RegexPattern = <(.*?)>
	 * While{
	 * line = readLine
	 * if line matches regexpattern #found a start/end SettingGroup
	 * if currentSettingGroup = null{
	 * 	currentSettingGroup = line trim(first 1) trim(last 1)
	 * 	continue
	 * }
	 * if currentSettingGroup equals line trim(first 2) trim (last 1){
	 * 	currentSettingGroup = null
	 * 	continue
	 * }
	 * #now we're not at a SettingGroup, extract info
	 * 
	 * items = line.split("=")
	 * items = [x.trim() for x in items]
	 * add to map key = items[0] value = items[1]
	 * 
	 * END
	 */
}
