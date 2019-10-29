/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.eclipse.collections.api.map.primitive.MutableShortObjectMap;
import org.eclipse.collections.api.set.primitive.MutableShortSet;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.ShortHashSet;
import org.snowjak.hivemind.Materials;
import org.snowjak.hivemind.Materials.Material;
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
		
		final MutableShortSet colorIndices = new ShortHashSet(), materialIndices = new ShortHashSet();
		for (int i = 0; i < src.getWidth(); i++)
			for (int j = 0; j < src.getHeight(); j++) {
				colorIndices.add(ColorCache.get().get(src.getForeground(i, j)));
				colorIndices.add(ColorCache.get().get(src.getBackground(i, j)));
				materialIndices.add(Materials.get().getIndex(src.getMaterial(i, j)));
			}
		
		final JsonObject colorMappings = new JsonObject();
		for (short index : colorIndices.toArray()) {
			if (index == -1)
				continue;
			colorMappings.add(Short.toString(index), context.serialize(ColorCache.get().get(index)));
		}
		obj.add("colors", colorMappings);
		
		final JsonObject materialMappings = new JsonObject();
		for (short index : materialIndices.toArray()) {
			if (index == -1)
				continue;
			materialMappings.add(Materials.get().get(index).getName(), new JsonPrimitive(index));
		}
		obj.add("material-mapping", materialMappings);
		
		obj.add("terrain", new JsonPrimitive(toBase64(src.getSquidCharMap())));
		obj.add("materials", new JsonPrimitive(toBase64(src.getMaterialIndexMap())));
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
		
		if (!obj.has("material-mapping"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [material-mapping]!");
		if (!obj.get("material-mapping").isJsonObject())
			throw new JsonParseException("Cannot parse GameMap from JSON -- [material-mapping] is not an object!");
		final JsonObject materialMappings = obj.getAsJsonObject("material-mapping");
		final MutableShortObjectMap<String> materialIndicesToNames = new ShortObjectHashMap<>();
		try {
			for (Entry<String, JsonElement> entry : materialMappings.entrySet()) {
				final short index = Short.parseShort(entry.getKey());
				final String name = entry.getValue().getAsString();
				materialIndicesToNames.put(index, name);
			}
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [material-mapping]!", t);
		}
		
		final char[][] squidCharMap;
		if (!obj.has("terrain"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [terrain]!");
		try {
			squidCharMap = toCharArray(obj.get("terrain").getAsString(), width, height);
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [terrain]!", t);
		}
		
		final short[][] materialsMap;
		final Material[][] translatedMaterials = new Material[width][height];
		if (!obj.has("materials"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [materials]!");
		try {
			materialsMap = toShortArray(obj.get("materials").getAsString(), width, height);
			
			for (int i = 0; i < width; i++)
				for (int j = 0; j < height; j++)
					translatedMaterials[i][j] = Materials.get().get(materialIndicesToNames.get(materialsMap[i][j]));
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [materials]!", t);
		}
		
		final GreasedRegion known;
		if (!obj.has("known"))
			throw new JsonParseException("Cannot parse GameMap from JSON -- missing [known]!");
		try {
			known = toGreasedRegion(obj.get("known").getAsString(), width, height);
		} catch (Throwable t) {
			throw new JsonParseException("Cannot parse GameMap from JSON -- cannot parse [known]!", t);
		}
		
		return new GameMap(squidCharMap, translatedMaterials, known, true);
	}
	
}
