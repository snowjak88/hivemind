/**
 * 
 */
package org.snowjak.hivemind.config;

import java.util.Properties;

import org.snowjak.hivemind.App;
import org.snowjak.hivemind.json.Json;

/**
 * Provides hardcoded default properties.
 * 
 * @author snowjak88
 *
 */
public class DefaultProperties {
	
	private static Properties properties = new Properties();
	{
		properties.put(App.PREFERENCE_WINDOW_WIDTH, 640);
		properties.put(App.PREFERENCE_WINDOW_HEIGHT, 480);
		properties.put(App.PREFERENCE_WINDOW_MIN_WIDTH, 640);
		properties.put(App.PREFERENCE_WINDOW_MIN_HEIGHT, 480);
		
		properties.put(Json.PREFRENCE_PRETTY_PRINTING, false);
	}
	
	public static Properties get() {
		
		return properties;
	}
}
