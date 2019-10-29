/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.systems.EntityRefManager;

import com.badlogic.ashley.core.Entity;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.SquidID;

/**
 * Special-case {@link Loader} to initialize the {@link Engine} currently held
 * in the {@link Context}.
 * 
 * @author snowjak88
 *
 */
public class EngineLoader implements Loader<Engine> {
	
	private static final Logger LOG = Logger.getLogger(EngineLoader.class.getName());
	
	@Override
	public JsonElement serialize(Engine src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonObject obj = new JsonObject();
		
		for (Entity e : src.getEntities()) {
			
			final EntityRefManager erm = src.getSystem(EntityRefManager.class);
			
			final String id = context.serialize(erm.get(e), SquidID.class).getAsString();
			obj.add(id, context.serialize(e, Entity.class));
		}
		
		return obj;
	}
	
	@Override
	public Engine deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot parse Engine from JSON -- not an object!");
		
		final JsonObject obj = json.getAsJsonObject();
		
		Context.get();
		final EntityRefManager erm = Context.getEngine().getSystem(EntityRefManager.class);
		
		for (Entry<String, JsonElement> entry : obj.entrySet()) {
			
			final SquidID id = context.deserialize(new JsonPrimitive(entry.getKey()), SquidID.class);
			final Entity e = context.deserialize(entry.getValue(), Entity.class);
			
			erm.add(e, id);
			
			Context.getEngine().addEntity(e);
			
		}
		
		LOG.info("Resolving references ...");
		while (!erm.resolveReferences()) {
			LOG.info("Still resolving references ...");
		}
		
		Context.get();
		return Context.getEngine();
	}
}
