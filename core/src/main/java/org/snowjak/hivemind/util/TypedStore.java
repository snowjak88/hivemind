/**
 * 
 */
package org.snowjak.hivemind.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author snowjak88
 *
 */
public class TypedStore {
	
	private static final Logger LOG = Logger.getLogger(TypedStore.class.getName());
	
	/**
	 * This store has specific support for these types. Be careful about using it
	 * for other types.
	 */
	public static final Collection<Class<?>> SUPPORTED_TYPES = Arrays.asList(String.class, Boolean.class, Integer.class,
			Float.class);
	
	protected final Map<String, TypedStoreItem<?>> configurations = new LinkedHashMap<>();
	
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
		
		synchronized (this) {
			if (!SUPPORTED_TYPES.stream().anyMatch(t -> t.isAssignableFrom(valueType)))
				LOG.warning(
						"WARNING: Registered item is not an explicitly-supported type (" + valueType.getName() + ")");
			
			final TypedStoreItem<T> ci = new TypedStoreItem<T>(valueType, key, description, defaultValue, configurable,
					requiresRestart, typeToString, stringToType);
			configurations.put(key, ci);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String key, Class<T> expectedType) {
		
		synchronized (this) {
			final TypedStoreItem<?> ci = configurations.get(key);
			if (ci == null)
				throw new IllegalArgumentException("Cannot get value for key [" + key + "] -- no such key recognized!");
			
			if (!ci.getType().isAssignableFrom(expectedType))
				throw new IllegalArgumentException("Cannot get value for key [" + key + "] -- item is a ["
						+ ci.getType().getSimpleName() + "], not a [" + expectedType.getSimpleName() + "]!");
			
			return ((TypedStoreItem<T>) ci).getValue();
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
			final TypedStoreItem<?> ci = configurations.get(key);
			if (ci == null)
				throw new IllegalArgumentException("Cannot set value for key [" + key + "] -- no such key registered!");
			
			if (!ci.getType().isAssignableFrom(value.getClass()))
				throw new IllegalArgumentException("Cannot set value for key [" + key + "] -- cannot assign a ["
						+ value.getClass().getSimpleName() + "] to a [" + ci.getType().getSimpleName() + "]!");
			
			((TypedStoreItem<T>) ci).setValue(value);
		}
	}
	
	public Collection<TypedStoreItem<?>> getItems() {
		
		synchronized (this) {
			return configurations.values();
		}
	}
	
	public TypedStoreItem<?> getItem(String key) {
		
		synchronized (this) {
			return configurations.get(key);
		}
	}
	
	public void clear() {
		
		synchronized (this) {
			configurations.clear();
		}
	}
	
	public static class TypedStoreItem<T> {
		
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
		 * Copies an existing TypedStoreItem.
		 * 
		 * @param toCopy
		 */
		public TypedStoreItem(TypedStoreItem<T> toCopy) {
			
			this(toCopy.valueType, toCopy.key, toCopy.description, toCopy.stringToType.apply(toCopy.defaultValue),
					toCopy.configurable, toCopy.requiresRestart, toCopy.typeToString, toCopy.stringToType);
			this.setValue(toCopy.stringToType.apply(toCopy.value));
		}
		
		TypedStoreItem(Class<T> type, String key, String description, T defaultValue, boolean configurable,
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
}
