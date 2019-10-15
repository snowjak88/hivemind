/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;
import java.util.Arrays;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.SquidID;

/**
 * @author snowjak88
 *
 */
public class SquidIDLoader implements Loader<SquidID> {
	
	@Override
	public JsonElement serialize(SquidID src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		return new JsonPrimitive(src.a + ":" + src.b + ":" + src.c + ":" + src.d);
	}
	
	@Override
	public SquidID deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonPrimitive())
			throw new JsonParseException("Cannot parse unique-ID from JSON -- not a primitive!");
		final String value = json.getAsString();
		
		final String[] split = value.split(":");
		
		if (split.length != 4)
			throw new JsonParseException("Cannot parse unique-ID from JSON -- ID doesn't have correct element-count!");
		
		try {
			final int[] ints = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
			
			return new SquidID(ints[0], ints[1], ints[2], ints[3]);
		} catch (NumberFormatException e) {
			throw new JsonParseException(
					"Cannot parse unique-ID from JSON -- at least one ID element is not parseable!");
		}
	}
	
}
