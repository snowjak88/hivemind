/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.RNG;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.systems.manager.EntityRefManager;

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
 * <p>
 * Note that this will <strong>also</strong> load/unload persisted state for the
 * following objects:
 * <ul>
 * <li>{@link RNG}</li>
 * </ul>
 * </p>
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
		
		final JsonObject rng = new JsonObject();
		rng.add("a", new JsonPrimitive(RNG.get().getStateA()));
		rng.add("b", new JsonPrimitive(RNG.get().getStateB()));
		obj.add("rng", rng);
		
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
		
		if (obj.has("rng") && obj.get("rng").isJsonObject()) {
			final JsonObject rng = obj.getAsJsonObject("rng");
			if (!rng.has("a"))
				throw new JsonParseException("Cannot parse Engine from JSON -- [rng] missing [a]!");
			if (!rng.has("b"))
				throw new JsonParseException("Cannot parse Engine from JSON -- [rng] missing [b]!");
			try {
				final int a = Integer.parseInt(rng.get("a").getAsString());
				final int b = Integer.parseInt(rng.get("b").getAsString());
				
				RNG.get().setState(a, b);
			} catch (NumberFormatException e) {
				throw new JsonParseException("Cannot parse Engine from JSON -- [rng] is malformed!", e);
			}
		}
		
		final EntityRefManager erm = Context.getEngine().getSystem(EntityRefManager.class);
		
		for (Entry<String, JsonElement> entry : obj.entrySet()) {
			
			if (entry.getKey().equalsIgnoreCase("rng"))
				continue;
			
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
