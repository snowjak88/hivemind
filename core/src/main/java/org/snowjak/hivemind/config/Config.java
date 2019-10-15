/**
 * 
 */
package org.snowjak.hivemind.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;
import java.util.logging.Logger;

import org.snowjak.hivemind.App;

/**
 * Abstracts configuration / preferences handling into a singleton instance.
 * 
 * @author snowjak88
 *
 */
public class Config {
	
	private static final Logger LOG = Logger.getLogger(Config.class.getName());
	
	private static final File configFile = new File(System.getProperty("user.home") + File.separator + ".prefs"
			+ File.separator + App.class.getPackage().getName() + ".properties");
	
	private static Config __INSTANCE = null;
	
	/**
	 * @return the singleton {@link Config} instance
	 */
	public static Config get() {
		
		if (__INSTANCE == null)
			synchronized (Config.class) {
				if (__INSTANCE == null)
					__INSTANCE = new Config();
			}
		return __INSTANCE;
	}
	
	private Properties properties = new Properties(DefaultProperties.get());
	
	private Config() {
		
		if (configFile.exists())
			try (FileReader fr = new FileReader(configFile)) {
				properties.load(fr);
			} catch (FileNotFoundException e) {
				LOG.warning("Could not find configuration file [" + configFile.getPath() + "].");
			} catch (IOException e) {
				LOG.severe("Could not load configuration file [" + configFile.getPath() + "]! -- "
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
				e.printStackTrace(System.err);
			}
	}
	
	/**
	 * Writes this Config to its properties-file.
	 */
	public void save() {
		
		try (FileWriter fw = new FileWriter(configFile, false)) {
			properties.store(fw, "Last saved: " + Instant.now().toString());
		} catch (IOException e) {
			LOG.severe("Could not save configuration file [" + configFile.getPath() + "]! -- "
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	
	public String getString(String key) {
		
		return getString(key, null);
	}
	
	public String getString(String key, String defaultValue) {
		
		return properties.getProperty(key, defaultValue);
	}
	
	public boolean getBoolean(String key) {
		
		return getBoolean(key, false);
	}
	
	public boolean getBoolean(String key, boolean defaultValue) {
		
		if (properties.containsKey(key))
			return Boolean.parseBoolean(properties.getProperty(key));
		
		return defaultValue;
	}
	
	public int getInt(String key) {
		
		return getInt(key, 0);
	}
	
	public int getInt(String key, int defaultValue) {
		
		if (properties.containsKey(key))
			return Integer.parseInt(properties.getProperty(key));
		
		return defaultValue;
	}
	
	public float getFloat(String key) {
		
		return getFloat(key, 0f);
	}
	
	public float getFloat(String key, float defaultValue) {
		
		if (properties.containsKey(key))
			return Float.parseFloat(properties.getProperty(key));
		
		return defaultValue;
	}
	
	public void set(String key, String value) {
		
		properties.setProperty(key, value);
	}
	
	public void set(String key, boolean value) {
		
		properties.setProperty(key, Boolean.toString(value));
	}
	
	public void set(String key, int value) {
		
		properties.setProperty(key, Integer.toString(value));
	}
	
	public void set(String key, float value) {
		
		properties.setProperty(key, Float.toString(value));
	}
	
}
