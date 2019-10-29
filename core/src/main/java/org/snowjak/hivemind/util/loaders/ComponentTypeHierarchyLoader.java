/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.Engine;

import com.badlogic.ashley.core.Component;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * <strong>Note</strong>: this <strong>assumes</strong> that the
 * {@link Component} being de-/serialized belongs to the {@link Engine}
 * currently held in the {@link Context}.
 * 
 * @author snowjak88
 *
 */
public class ComponentTypeHierarchyLoader implements TypeHierarchyLoader<Component> {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(ComponentTypeHierarchyLoader.class.getName());
	
	@Override
	public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonObject obj = new JsonObject();
		
		obj.add("__type", new JsonPrimitive(src.getClass().getName()));
		
		for (Field f : src.getClass().getDeclaredFields()) {
			try {
				
				if (!Modifier.isTransient(f.getModifiers()) && !f.isAnnotationPresent(IgnoreSerialization.class))
					obj.add(f.getName(), context.serialize(f.get(src)));
				
			} catch (IllegalAccessException e) {
				throw new JsonParseException(
						"Cannot serialize [" + src.getClass().getName() + "].[" + f.getName() + "]", e);
			}
		}
		
		return obj;
	}
	
	@Override
	public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot parse Component from JSON: not an object!");
		
		final JsonObject obj = json.getAsJsonObject();
		
		if (!obj.has("__type"))
			throw new JsonParseException("Cannot parse Component from JSON: missing [__type]!");
		
		try {
			@SuppressWarnings("unchecked")
			final Class<? extends Component> clazz = (Class<? extends Component>) Class
					.forName(obj.get("__type").getAsString());
			
			try {
				
				final Object result = Context.getEngine().createComponent(clazz);
				
				for (Field f : clazz.getDeclaredFields()) {
					
					if (!Modifier.isTransient(f.getModifiers()) && !f.isAnnotationPresent(IgnoreSerialization.class))
						if (!obj.has(f.getName()))
							throw new JsonParseException(
									"Cannot parse Component(" + clazz.getSimpleName() + ") from JSON");
						else
							f.set(result, context.deserialize(obj.get(f.getName()), f.getType()));
				}
				
				return (Component) result;
				
			} catch (IllegalAccessException e) {
				throw new JsonParseException("Cannot deserialize [" + clazz.getName() + "]", e);
			}
			
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Very* unexpected exception while deserializing Component!", e);
		}
	}
	
}
