/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;
import java.util.Map.Entry;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.systems.manager.EntityRefManager;
import org.snowjak.hivemind.map.EntityMap;

import com.badlogic.ashley.core.Entity;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.SquidID;

/**
 * @author snowjak88
 *
 */
public class EntityMapLoader implements Loader<EntityMap> {
	
	@Override
	public JsonElement serialize(EntityMap src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonObject obj = new JsonObject();
		
		final EntityRefManager erm = Context.getEngine().getSystem(EntityRefManager.class);
		
		for (Coord c : src.getLocations()) {
			final OrderedSet<Entity> at = src.getAt(c);
			if (at.isEmpty())
				continue;
			
			final String key = toString(c);
			final JsonArray entityIDs = new JsonArray();
			for (Entity e : at)
				entityIDs.add(context.serialize(erm.get(e)));
			obj.add(key, entityIDs);
		}
		
		return obj;
	}
	
	@Override
	public EntityMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot parse EntityMap from JSON -- not an object!");
		
		final EntityRefManager erm = Context.getEngine().getSystem(EntityRefManager.class);
		
		final EntityMap result = new EntityMap();
		final JsonObject obj = json.getAsJsonObject();
		
		for (Entry<String, JsonElement> entry : obj.entrySet()) {
			try {
				
				final Coord c = toCoord(entry.getKey());
				
				if (!entry.getValue().isJsonArray())
					throw new JsonParseException(
							"Cannot parse EntityMap from JSON -- expected an array of entity-IDs associated with this coordinate ("
									+ c.x + "," + c.y + ")!");
				
				final JsonArray entityIDs = entry.getValue().getAsJsonArray();
				
				for (JsonElement entityID : entityIDs) {
					final SquidID id = context.deserialize(entityID, SquidID.class);
					erm.addReferenceResolution(id, (e) -> result.set(c, e));
				}
				
			} catch (Throwable t) {
				throw new JsonParseException("Cannot parse EntityMap from JSON!", t);
			}
		}
		
		erm.resolveReferences();
		
		return result;
	}
	
	private String toString(Coord c) {
		
		return c.toString();
	}
	
	private Coord toCoord(String s) {
		
		if (!s.startsWith("(") || !s.endsWith(")"))
			throw new IllegalArgumentException("Cannot parse String as Coord -- missing parentheses!");
		
		s = s.substring(1, s.length() - 1);
		
		final String[] pieces = s.split(",");
		if (pieces.length != 2)
			throw new IllegalArgumentException("Cannot parse String as Coord -- incorrect number of indices!");
		
		final int x = Integer.parseInt(pieces[0]), y = Integer.parseInt(pieces[1]);
		
		return Coord.get(x, y);
	}
}
