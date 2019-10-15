/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.Coord;

public class CoordLoader implements Loader<Coord> {
	
	@Override
	public Coord deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonArray())
			throw new JsonParseException("Cannot parse Coord from JSON -- not an array!");
		
		final JsonArray array = json.getAsJsonArray();
		
		if (array.size() != 2)
			throw new JsonParseException("Cannot parse Coord from JSON -- incorrect size!");
		
		return Coord.get(array.get(0).getAsInt(), array.get(1).getAsInt());
	}
	
	@Override
	public JsonElement serialize(Coord src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonArray array = new JsonArray();
		
		array.add(new JsonPrimitive(src.x));
		array.add(new JsonPrimitive(src.y));
		
		return array;
	}
}