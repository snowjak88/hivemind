/**
 * 
 */
package org.snowjak.hivemind.config;

import java.util.function.Function;

/**
 * @author snowjak88
 *
 */
public class ConfigurationItem<T> {
	
	private final Class<T> valueType;
	private final String key;
	private final String description;
	private final String defaultValue;
	private final boolean configurable;
	private final boolean requiresRestart;
	private final Function<T, String> typeToString;
	private final Function<String, T> stringToType;
	private String value;
	
	/**
	 * Copies an existing ConfigurationItem.
	 * 
	 * @param toCopy
	 */
	public ConfigurationItem(ConfigurationItem<T> toCopy) {
		
		this(toCopy.valueType, toCopy.key, toCopy.description, toCopy.stringToType.apply(toCopy.defaultValue),
				toCopy.configurable, toCopy.requiresRestart, toCopy.typeToString, toCopy.stringToType);
		this.setValue(toCopy.stringToType.apply(toCopy.value));
	}
	
	ConfigurationItem(Class<T> type, String key, String description, T defaultValue, boolean configurable,
			boolean requiresRestart, Function<T, String> typeToString, Function<String, T> stringToType) {
		
		this.valueType = type;
		this.key = key;
		this.description = description;
		this.defaultValue = typeToString.apply(defaultValue);
		this.configurable = configurable;
		this.requiresRestart = requiresRestart;
		this.typeToString = typeToString;
		this.stringToType = stringToType;
		
		this.value = typeToString.apply(defaultValue);
	}
	
	public Class<T> getType() {
		
		return valueType;
	}
	
	public T getValue() {
		
		return stringToType.apply(value);
	}
	
	public String getStringValue() {
		
		return value;
	}
	
	public void setStringValue(String value) {
		
		this.value = value;
	}
	
	public void setValue(T value) {
		
		this.value = typeToString.apply(value);
	}
	
	public String getKey() {
		
		return key;
	}
	
	public String getDescription() {
		
		return description;
	}
	
	public T getDefaultValue() {
		
		return stringToType.apply(defaultValue);
	}
	
	public boolean isConfigurable() {
		
		return configurable;
	}
	
	public boolean isRequiresRestart() {
		
		return requiresRestart;
	}
}
