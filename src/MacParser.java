import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Class designed specifically to parse the settings information stored in a
 * .mac file. The .mac file extension is purely for satire and does not follow
 * and previously conceived style definitions (although it is inspired by xml and
 * simplified from that format). The files are laid out in this manner:
 * 
 * <SettingClass>
 * key1 = value1
 * key2 = value2
 * key3 = value3
 * ...
 * </SettingClass>
 * 
 * As many SettingClasses as necessary may be used, but they should be escaped.
 * 
 * @author jkarnuta
 *
 */
public class MacParser {

	final String filePath = "/Resources/YTDLSettings.mac";
	private InputStream fileIS;
	private Logger logger;
	/*
	 * mapOfMaps maps the SettingClass to the HashMap containing the Key : Value pairs 
	 * for the options inside the map
	 */
	private HashMap<String, HashMap<String, String>> mapOfMaps;
	
	/**
	 * Constructor
	 */
	public MacParser(){
		this.logger = null;
		this.mapOfMaps = new HashMap<String,HashMap<String, String>>();
	}
	
	/**
	 * Adds a logging object if wanted (no calls will be made unless the logger is specified)
	 * @param logger
	 */
	public void setLogger(Logger logger){
		this.logger = logger;
	}
	
	/**
	 * Returns the SettingClass fields of the map
	 * @return the SettingClass fields
	 */
	public Set<String> getSettingsFields(){
		return mapOfMaps.keySet();
	}
	
	/**
	 * Gets the options (keys) of the specified SettingClass
	 * @param SettingClass to get optionss from
	 * @return Set of options
	 */
	public Set<String> getOptions(String SettingClass){
		if(!mapOfMaps.keySet().contains(SettingClass)) return new HashSet<String>();
		return mapOfMaps.get(SettingClass).keySet();
	}
	
	/**
	 * Gets the value associated with the specified option inside the SettingClass
	 * @param SettingClass
	 * @param Key = Option
	 * @return value
	 */
	public String getValue(String SettingClass, String Key){
		return mapOfMaps.get(SettingClass).get(Key);
	}
	
	/**
	 * Sets the value of the Key inside the specified SettingClass
	 * @param SettingClass
	 * @param Key
	 * @param Value
	 */
	public void setValue(String SettingClass, String Key, String Value){
		mapOfMaps.get(SettingClass).put(Key, Value);
	}

	private void getInputStreamFromPath(){
		try {
			fileIS = this.getClass().getResource(filePath).openStream();
		} catch (IOException e) {
			logger.severe(e.getMessage());
		}
	}
	public static void main(String[] args){
	}
}
