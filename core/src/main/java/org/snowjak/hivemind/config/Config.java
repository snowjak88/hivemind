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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Abstracts configuration / preferences handling into a singleton instance.
 * 
 * @author snowjak88
 *
 */
public class Config {
	
	private static final Logger LOG = Logger.getLogger(Config.class.getName());
	
	private static final File CONFIG_FILE = new File("hivemind.properties");
	
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
	
	private final Map<String, ConfigurationItem<?>> configurations = new LinkedHashMap<>();
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
	
	public void register(String key, String description, boolean defaultValue, boolean configurable,
			boolean requiresRestart) {
		
		register(Boolean.class, key, description, defaultValue, configurable, requiresRestart,
				(v) -> Boolean.toString(v), Boolean::parseBoolean);
	}
	
	public void register(String key, String description, int defaultValue, boolean configurable,
			boolean requiresRestart) {
		
		register(Integer.class, key, description, defaultValue, configurable, requiresRestart,
				(v) -> Integer.toString(v), Integer::parseInt);
	}
	
	public void register(String key, String description, float defaultValue, boolean configurable,
			boolean requiresRestart) {
		
		register(Float.class, key, description, defaultValue, configurable, requiresRestart, (v) -> Float.toString(v),
				Float::parseFloat);
	}
	
	public void register(String key, String description, String defaultValue, boolean configurable,
			boolean requiresRestart) {
		
		register(String.class, key, description, defaultValue, configurable, requiresRestart, (s) -> s, (s) -> s);
	}
	
	public <T> void register(Class<T> valueType, String key, String description, T defaultValue, boolean configurable,
			boolean requiresRestart, Function<T, String> typeToString, Function<String, T> stringToType) {
		
		final ConfigurationItem<T> ci = new ConfigurationItem<T>(valueType, key, description, defaultValue,
				configurable, requiresRestart, typeToString, stringToType);
		final String existingValue = properties.getProperty(key);
		if (existingValue != null)
			ci.setStringValue(properties.getProperty(key));
		configurations.put(key, ci);
	}
	
	/**
	 * Writes this Config to its properties-file.
	 */
	public void save() {
		
		for (ConfigurationItem<?> ci : configurations.values())
			properties.setProperty(ci.getKey(), ci.getStringValue());
		
		try (FileWriter fw = new FileWriter(CONFIG_FILE, false)) {
			properties.store(fw, "Last saved: " + Instant.now().toString());
		} catch (IOException e) {
			LOG.severe("Could not save configuration file [" + CONFIG_FILE.getPath() + "]! -- "
					+ e.getClass().getSimpleName() + ": " + e.getMessage());
			e.printStackTrace(System.err);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> expectedType) {
		
		synchronized (this) {
			final ConfigurationItem<?> ci = configurations.get(key);
			if (ci == null)
				throw new IllegalArgumentException(
						"Cannot get value from configuration [" + key + "] -- no such key recognized!");
			
			if (!ci.getType().isAssignableFrom(expectedType))
				throw new IllegalArgumentException(
						"Cannot get value from configuration [" + key + "] -- configuration-item is a ["
								+ ci.getType().getSimpleName() + "], not a [" + expectedType.getSimpleName() + "]!");
			
			return ((ConfigurationItem<T>) ci).getValue();
		}
	}
	
	public String get(String key) {
		
		return get(key, String.class);
	}
	
	public boolean getBoolean(String key) {
		
		return get(key, Boolean.class);
	}
	
	public int getInt(String key) {
		
		return get(key, Integer.class);
	}
	
	public float getFloat(String key) {
		
		return get(key, Float.class);
	}
	
	@SuppressWarnings("unchecked")
	public <T> void set(String key, T value) {
		
		synchronized (this) {
			final ConfigurationItem<?> ci = configurations.get(key);
			if (ci == null)
				throw new IllegalArgumentException(
						"Cannot set value for configuration [" + key + "] -- no such configuration registered!");
			
			if (!ci.getType().isAssignableFrom(value.getClass()))
				throw new IllegalArgumentException(
						"Cannot set value for configuration [" + key + "] -- cannot assign a ["
								+ value.getClass().getSimpleName() + "] to a [" + ci.getType().getSimpleName() + "]!");
			
			((ConfigurationItem<T>) ci).setValue(value);
		}
	}
	
	public Collection<ConfigurationItem<?>> getConfigurations() {
		
		return configurations.values();
	}
	
	public ConfigurationItem<?> getConfiguration(String key) {
		
		return configurations.get(key);
	}
}
