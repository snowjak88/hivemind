/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Type;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Factions;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.systems.manager.EntityRefManager;
import org.snowjak.hivemind.engine.systems.manager.FactionManager;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;

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

import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.SquidID;

/**
 * <strong>Note</strong>: this <strong>assumes</strong> that the {@link Entity}
 * being de-/serialized belongs to the {@link Engine} currently held in the
 * {@link Context}.
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
		
		final EntityRefManager ref = Context.getEngine().getSystem(EntityRefManager.class);
		if (ref != null)
			obj.add("id", context.serialize(ref.get(src), SquidID.class));
		
		final UniqueTagManager utm = Context.getEngine().getSystem(UniqueTagManager.class);
		if (utm != null && utm.has(src)) {
			final JsonArray tags = new JsonArray();
			final OrderedSet<String> tagValues = utm.get(src);
			for (int i = 0; i < tagValues.size(); i++)
				tags.add(tagValues.getAt(i));
			obj.add("tags", tags);
		}
		
		final FactionManager fm = Context.getEngine().getSystem(FactionManager.class);
		if (fm != null && fm.has(src))
			obj.add("faction", new JsonPrimitive(fm.get(src).getName()));
		
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
		
		final Entity e = Context.getEngine().createEntity();
		
		if (obj.has("id")) {
			final EntityRefManager erm = Context.getEngine().getSystem(EntityRefManager.class);
			if (erm != null)
				erm.add(e, context.deserialize(obj.get("id"), SquidID.class));
		}
		
		if (obj.has("tags")) {
			final UniqueTagManager utm = Context.getEngine().getSystem(UniqueTagManager.class);
			if (utm != null) {
				if (!obj.get("tags").isJsonArray())
					throw new JsonParseException("Cannot parse Entity from JSON -- [tags] is not an array!");
				final JsonArray tags = obj.getAsJsonArray("tags");
				tags.forEach(je -> utm.set(je.getAsString(), e));
			}
		}
		
		if (obj.has("faction")) {
			final FactionManager fm = Context.getEngine().getSystem(FactionManager.class);
			if (fm != null)
				fm.set(Factions.get().getBy(obj.get("faction").getAsString()), e);
		}
		
		if (obj.has("components")) {
			final JsonArray components = obj.getAsJsonArray("components");
			components.forEach(je -> e.add(context.deserialize(je, Component.class)));
		}
		
		return e;
	}
	
}
