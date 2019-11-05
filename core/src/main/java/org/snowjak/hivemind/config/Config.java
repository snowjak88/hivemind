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
import java.util.function.Function;
import java.util.logging.Logger;

import org.snowjak.hivemind.util.TypedStore;

/**
 * Abstracts configuration / preferences handling into a singleton instance.
 * 
 * @author snowjak88
 *
 */
public class Config extends TypedStore {
	
	private static final Logger LOG = Logger.getLogger(Config.class.getName());
	
	/**
	 * Application configuration-values are stored in this file.
	 */
	public static final File CONFIG_FILE = new File("hivemind.properties");
	
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
	
	private final Properties properties = new Properties();
	
	private Config() {
		
		if (CONFIG_FILE.exists()) {
			try (FileReader fr = new FileReader(CONFIG_FILE)) {
				properties.load(fr);
			} catch (FileNotFoundException e) {
				LOG.warning("Could not find configuration file [" + CONFIG_FILE.getPath() + "].");
			} catch (IOException e) {
				LOG.severe("Could not load configuration file [" + CONFIG_FILE.getPath() + "]! -- "
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
	}
	
	/**
	 * Writes this Config to its properties-file.
	 */
	public void save() {
		
		synchronized (this) {
			for (TypedStoreItem<?> ci : configurations.values())
				properties.setProperty(ci.getKey(), ci.getStringValue());
			
			try (FileWriter fw = new FileWriter(CONFIG_FILE, false)) {
				properties.store(fw, "Last saved: " + Instant.now().toString());
			} catch (IOException e) {
				LOG.severe("Could not save configuration file [" + CONFIG_FILE.getPath() + "]! -- "
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
				e.printStackTrace(System.err);
			}
		}
	}
	
	@Override
	public <T> void register(Class<T> valueType, String key, String description, T defaultValue, boolean configurable,
			boolean requiresRestart, Function<T, String> typeToString, Function<String, T> stringToType) {
		
		synchronized (this) {
			super.register(valueType, key, description, defaultValue, configurable, requiresRestart, typeToString,
					stringToType);
			
			final String existingValue = properties.getProperty(key);
			if (existingValue != null)
				getItem(key).setStringValue(properties.getProperty(key));
		}
	}
	
	@Override
	public void clear() {
		
		synchronized (this) {
			super.clear();
			properties.clear();
		}
	}
}
