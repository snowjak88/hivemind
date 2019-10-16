/**
 * 
 */
package org.snowjak.hivemind.config;

import java.util.Properties;

import org.snowjak.hivemind.App;
import org.snowjak.hivemind.display.Fonts;
import org.snowjak.hivemind.json.Json;

/**
 * Provides hardcoded default properties.
 * 
 * @author snowjak88
 *
 */
public class DefaultProperties {
	
	private static Properties properties = null;
	
	public static Properties get() {
		
		if (properties == null)
			synchronized (Properties.class) {
				if (properties == null) {
					properties = new Properties();
					properties.setProperty(App.PREFERENCE_WINDOW_WIDTH, Integer.toString(800));
					properties.setProperty(App.PREFERENCE_WINDOW_HEIGHT, Integer.toString(600));
					properties.setProperty(App.PREFERENCE_WINDOW_MIN_WIDTH, Integer.toString(800));
					properties.setProperty(App.PREFERENCE_WINDOW_MIN_HEIGHT, Integer.toString(600));
					
					properties.setProperty(Fonts.FONT_NORMAL, "SMOOTH");
					properties.setProperty(Fonts.FONT_HEADING, "LARGE");
					properties.setProperty(Fonts.FONT_MAP, "SQUARE");
					
					properties.setProperty(Json.PREFRENCE_PRETTY_PRINTING, Boolean.toString(false));
				}
			}
		
		return properties;
	}
}
