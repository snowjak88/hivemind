/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;

import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.systems.EntityRefManager;
import org.snowjak.hivemind.engine.systems.UniqueTagManager;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.SquidID;

/**
 * <strong>Note</strong>: this <strong>assumes</strong> that the {@link Entity}
 * being de-/serialized belongs to {@link Engine#get() the singleton Engine
 * instance}.
 * 
 * @author snowjak88
 *
 */
public class EntityLoader implements Loader<Entity> {
	
	@Override
	public JsonElement serialize(Entity src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonObject obj = new JsonObject();
		
		final EntityRefManager ref = Engine.get().getSystem(EntityRefManager.class);
		if (ref != null)
			obj.add("id", context.serialize(ref.get(src), SquidID.class));
		
		final UniqueTagManager utm = Engine.get().getSystem(UniqueTagManager.class);
		if (utm != null && utm.has(src))
			obj.add("tag", new JsonPrimitive(utm.get(src)));
		
		final JsonArray components = new JsonArray();
		for (Component c : src.getComponents())
			if (!c.getClass().isAnnotationPresent(IgnoreSerialization.class))
				components.add(context.serialize(c, Component.class));
			
		obj.add("components", components);
		
		return obj;
	}
	
	@Override
	public Entity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot parse Entity from JSON -- not an object!");
		
		final JsonObject obj = json.getAsJsonObject();
		
		final Entity e = Engine.get().createEntity();
		
		if (obj.has("id")) {
			final EntityRefManager erm = Engine.get().getSystem(EntityRefManager.class);
			if (erm != null)
				erm.add(e, context.deserialize(obj.get("id"), SquidID.class));
		}
		
		if (obj.has("tag")) {
			final UniqueTagManager utm = Engine.get().getSystem(UniqueTagManager.class);
			if (utm != null)
				utm.set(obj.get("tag").getAsString(), e);
		}
		
		if (obj.has("components")) {
			final JsonArray components = obj.getAsJsonArray("components");
			components.forEach(je -> e.add(context.deserialize(je, Component.class)));
		}
		
		return e;
	}
	
}
