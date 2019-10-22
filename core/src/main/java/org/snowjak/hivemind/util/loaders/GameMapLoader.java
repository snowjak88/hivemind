/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;

import org.snowjak.hivemind.map.GameMap;

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
		
		// final MutableShortSet colorIndices = new ShortHashSet();
		// for (int i = 0; i < src.getWidth(); i++)
		// for (int j = 0; j < src.getHeight(); j++) {
		// colorIndices.add(src.getForegroundIndex(i, j));
		// colorIndices.add(src.getBackgroundIndex(i, j));
		// }
		//
		// final JsonObject colorMappings = new JsonObject();
		//
		// for (short index : colorIndices.toArray()) {
		// if (index == -1)
		// continue;
		//
		// colorMappings.add(Short.toString(index),
		// context.serialize(ColorCache.get().get(index)));
		// }
		//
		// obj.add("colors", colorMappings);
		
		obj.add("terrain", new JsonPrimitive(toBase64(src.getSquidCharMap())));
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
		
		// if (!obj.has("colors"))
		// throw new JsonParseException("Cannot parse GameMap from JSON -- missing
		// [colors]!");
		// if (!obj.get("colors").isJsonObject())
		// throw new JsonParseException("Cannot parse GameMap from JSON -- [colors] is
		// not an object!");
		// final JsonObject colorMappings = obj.getAsJsonObject("colors");
		// try {
		// for (Entry<String, JsonElement> entry : colorMappings.entrySet()) {
		// final short index = Short.parseShort(entry.getKey());
		// ColorCache.get().set(context.deserialize(entry.getValue(), Color.class),
		// index);
		// }
		// } catch (Throwable t) {
		// throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse
		// [colors]!", t);
		// }
		
		final char[][] squidCharMap;
		if (!obj.has("terrain"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [terrain]!");
		try {
			squidCharMap = toCharArray(obj.get("terrain").getAsString(), width, height);
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [terrain]!", t);
		}
		
		final GreasedRegion known;
		if (!obj.has("known"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [known]!");
		try {
			known = toGreasedRegion(obj.get("known").getAsString(), width, height);
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [known]!", t);
		}
		
		return new GameMap(squidCharMap, known, true);
	}
	
}
