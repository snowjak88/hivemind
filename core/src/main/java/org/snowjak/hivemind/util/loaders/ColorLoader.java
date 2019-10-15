/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * @author snowjak88
 *
 */
public class ColorLoader implements Loader<Color> {
	
	@Override
	public Color deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		return Color.valueOf(json.getAsString());
	}
	
	@Override
	public JsonElement serialize(Color src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		return new JsonPrimitive(
				(src instanceof SColor) ? src.toString().replaceAll(((SColor) src).getName(), "").trim()
						: src.toString());
	}
	
}
