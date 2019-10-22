/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.eclipse.collections.api.set.primitive.MutableShortSet;
import org.eclipse.collections.impl.set.mutable.primitive.ShortHashSet;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.cache.ColorCache;

import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.GreasedRegion;

/**
 * @author snowjak88
 *
 */
public class GameMapLoader implements Loader<GameMap> {
	
	@Override
	public JsonElement serialize(GameMap src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonObject obj = new JsonObject();
		
		obj.add("width", new JsonPrimitive(src.getWidth()));
		obj.add("height", new JsonPrimitive(src.getHeight()));
		
		final MutableShortSet colorIndices = new ShortHashSet();
		for (int i = 0; i < src.getWidth(); i++)
			for (int j = 0; j < src.getHeight(); j++) {
				colorIndices.add(src.getForegroundIndex(i, j));
				colorIndices.add(src.getBackgroundIndex(i, j));
			}
		
		final JsonObject colorMappings = new JsonObject();
		
		for (short index : colorIndices.toArray()) {
			if (index == -1)
				continue;
			
			colorMappings.add(Short.toString(index), context.serialize(ColorCache.get().get(index)));
		}
		
		obj.add("colors", colorMappings);
		
		obj.add("chars", new JsonPrimitive(toBase64(src.getChars())));
		obj.add("foreground", new JsonPrimitive(toBase64(src.getForegroundIndices())));
		obj.add("background", new JsonPrimitive(toBase64(src.getBackgroundIndices())));
		obj.add("known", new JsonPrimitive(toBase64(src.getKnown())));
		
		return obj;
	}
	
	@Override
	public GameMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot parse GameMap from JSON -- not an object!");
		
		final JsonObject obj = json.getAsJsonObject();
		
		final int width;
		if (!obj.has("width"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [width]!");
		try {
			width = Integer.parseInt(obj.get("width").getAsString());
		} catch (NumberFormatException e) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- [width] cannot be parsed as an integer!",
					e);
		}
		
		final int height;
		if (!obj.has("height"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [height]!");
		try {
			height = Integer.parseInt(obj.get("height").getAsString());
		} catch (NumberFormatException e) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- [height] cannot be parsed as an integer!",
					e);
		}
		
		if (!obj.has("colors"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [colors]!");
		if (!obj.get("colors").isJsonObject())
			throw new JsonParseException("Cannot parse GameMap from JSON -- [colors] is not an object!");
		final JsonObject colorMappings = obj.getAsJsonObject("colors");
		try {
			for (Entry<String, JsonElement> entry : colorMappings.entrySet()) {
				final short index = Short.parseShort(entry.getKey());
				ColorCache.get().set(context.deserialize(entry.getValue(), Color.class), index);
			}
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [colors]!", t);
		}
		
		final char[][] chars;
		if (!obj.has("chars"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [chars]!");
		try {
			chars = toCharArray(obj.get("chars").getAsString(), width, height);
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [chars]!", t);
		}
		
		final short[][] foreground;
		if (!obj.has("foreground"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [foreground]!");
		try {
			foreground = toShortArray(obj.get("foreground").getAsString(), width, height);
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [foreground]!", t);
		}
		
		final short[][] background;
		if (!obj.has("background"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [background]!");
		try {
			background = toShortArray(obj.get("background").getAsString(), width, height);
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [background]!", t);
		}
		
		final GreasedRegion known;
		if (!obj.has("known"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [known]!");
		try {
			known = toGreasedRegion(obj.get("known").getAsString(), width, height);
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [known]!", t);
		}
		
		return new GameMap(chars, GameMap.uncompressColors(foreground), GameMap.uncompressColors(background), known);
	}
	
}
