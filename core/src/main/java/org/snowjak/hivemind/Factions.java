/**
 * 
 */
package org.snowjak.hivemind;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.eclipse.collections.api.map.primitive.MutableObjectShortMap;
import org.eclipse.collections.api.map.primitive.MutableShortFloatMap;
import org.eclipse.collections.api.map.primitive.MutableShortObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectShortHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortFloatHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ShortObjectHashMap;
import org.snowjak.hivemind.json.Json;
import org.snowjak.hivemind.util.loaders.Loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.OrderedSet;

/**
 * Repository for factions.
 * 
 * @author snowjak88
 *
 */
public class Factions {
	
	private static final Logger LOG = Logger.getLogger(Factions.class.getName());
	private static final String FILE_NAME = "data/factions.json";
	
	private static Factions __INSTANCE = null;
	
	public static Factions get() {
		
		if (__INSTANCE == null) {
			if (Gdx.files == null)
				throw new IllegalStateException("Cannot load available factions -- LibGDX not yet initialized!");
			final File factionsFile = Gdx.files.local(FILE_NAME).file();
			try (FileReader fr = new FileReader(factionsFile)) {
				
				__INSTANCE = Json.get().fromJson(fr, Factions.class);
				
			} catch (IOException e) {
				LOG.severe("Cannot read factions file -- " + e.getClass().getSimpleName() + ": " + e.getMessage());
				throw new RuntimeException(e);
			}
		}
		
		return __INSTANCE;
	}
	
	private final OrderedSet<Faction> allTypes = new OrderedSet<>();
	private final MutableObjectShortMap<Faction> typesToIndices = new ObjectShortHashMap<>();
	private final Map<String, Faction> namesToTypes = new HashMap<>();
	
	private final MutableShortObjectMap<MutableShortFloatMap> relations = new ShortObjectHashMap<>();
	
	private Factions() {
		
	}
	
	public Faction getDefault() {
		
		return getBy("default");
	}
	
	/**
	 * Get all registered {@link Faction}s.
	 * 
	 * @return
	 */
	public OrderedSet<Faction> getAll() {
		
		return allTypes;
	}
	
	/**
	 * Register the given {@link Faction}. If the given Faction has already been
	 * registered, this method has no effect.
	 * 
	 * @param type
	 */
	public void register(Faction type) {
		
		if (type == null)
			throw new NullPointerException("Cannot register a {null} Faction!");
		
		synchronized (this) {
			if (allTypes.contains(type))
				return;
			
			allTypes.add(type);
			final short newIndex = (short) allTypes.indexOf(type);
			
			namesToTypes.put(type.name.toLowerCase(), type);
			typesToIndices.put(type, newIndex);
		}
	}
	
	/**
	 * Updates the relation-value with which the first {@link Faction} regards the
	 * second.
	 * 
	 * @param from
	 * @param to
	 * @param relation
	 */
	public void updateRelation(Faction from, Faction to, float relation) {
		
		if (from == null || to == null)
			throw new NullPointerException("Cannot update relations between {null} Factions!");
		
		synchronized (this) {
			final short fromIndex = getIndexOf(from);
			final short toIndex = getIndexOf(to);
			
			relations.getIfAbsentPut(fromIndex, new ShortFloatHashMap()).put(toIndex, relation);
		}
	}
	
	/**
	 * Get the relation-value with which the first {@link Faction} regards the
	 * second. (By default, this is {@code 0.0})
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public float getRelation(Faction from, Faction to) {
		
		if (from == null || to == null)
			throw new NullPointerException("Cannot retrieve relations between {null} Factions!");
		
		synchronized (this) {
			final short fromIndex = getIndexOf(from);
			final short toIndex = getIndexOf(to);
			
			return relations.getIfAbsentPut(fromIndex, new ShortFloatHashMap()).getIfAbsentPut(toIndex, 0f);
		}
	}
	
	/**
	 * Get the index corresponding to the given {@link Faction}. If this Faction has
	 * not yet been registered, this will also automatically register that type.
	 * 
	 * @param type
	 * @return -1, if {@code type == null}
	 */
	public short getIndexOf(Faction type) {
		
		if (type == null)
			return -1;
		
		synchronized (this) {
			if (!typesToIndices.containsKey(type))
				register(type);
			
			return typesToIndices.get(type);
		}
	}
	
	/**
	 * Get the index corresponding to the {@link Faction} associated with the given
	 * name.
	 * 
	 * @param name
	 * @return
	 */
	public short getIndexOf(String name) {
		
		return getIndexOf(getBy(name));
	}
	
	/**
	 * Get the {@link Faction} corresponding to the given index.
	 * 
	 * @param index
	 * @return {@code null} if no such Faction has been registered
	 */
	public Faction getAt(short index) {
		
		synchronized (this) {
			return allTypes.getAt(index);
		}
	}
	
	/**
	 * Get the {@link Faction} corresponding to the given name.
	 * 
	 * @param name
	 * @return {@code null} if no such Faction has been registered
	 */
	public Faction getBy(String name) {
		
		synchronized (this) {
			return namesToTypes.get(name);
		}
	}
	
	/**
	 * Given another {@link Factions} instance -- say, one deserialized from JSON --
	 * update <em>this</em> Factions instance with the values held in the other.
	 * instance.
	 * 
	 * @param json
	 * @param context
	 */
	public void updateFrom(Factions other) {
		
		synchronized (this) {
			for (Faction otherFaction : other.allTypes)
				this.register(otherFaction);
			
			for (Faction fromOther : other.allTypes)
				for (Faction toOther : other.allTypes)
					this.updateRelation(fromOther, toOther, other.getRelation(fromOther, toOther));
		}
	}
	
	public static class Faction {
		
		private String name = null;
		private Color color = null;
		
		public String getName() {
			
			return name;
		}
		
		public void setName(String name) {
			
			this.name = name;
		}
		
		public Color getColor() {
			
			return color;
		}
		
		public void setColor(Color color) {
			
			this.color = color;
		}
		
	}
	
	public static class FactionsLoader implements Loader<Factions> {
		
		@Override
		public JsonElement serialize(Factions src, Type typeOfSrc, JsonSerializationContext context) {
			
			if (src == null)
				return JsonNull.INSTANCE;
			
			final JsonObject obj = new JsonObject();
			
			final JsonArray factions = new JsonArray();
			
			for (Faction type : src.allTypes)
				factions.add(context.serialize(type));
			
			obj.add("factions", factions);
			
			final JsonObject relations = new JsonObject();
			
			for (Faction type : src.allTypes) {
				final JsonObject relation = new JsonObject();
				
				for (Faction other : src.allTypes) {
					final float relationValue = src.getRelation(type, other);
					if (relationValue != 0)
						relation.add(other.name, new JsonPrimitive(relationValue));
				}
				
				relations.add(type.name, relation);
			}
			
			obj.add("relations", relations);
			
			return obj;
		}
		
		@Override
		public Factions deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (json.isJsonNull())
				return null;
			
			if (!json.isJsonObject())
				throw new JsonParseException("Cannot parse factions -- expecting an object!");
			
			final JsonObject obj = json.getAsJsonObject();
			
			final Factions result = new Factions();
			
			if (!obj.has("factions"))
				throw new JsonParseException("Cannot parse factions -- missing [factions]!");
			
			obj.getAsJsonArray("factions").forEach(je -> result.register(context.deserialize(je, Faction.class)));
			
			if (!obj.has("relations"))
				throw new JsonParseException("Cannot parse factions -- missing [relations]!");
			if (!obj.get("relations").isJsonObject())
				throw new JsonParseException("Cannot parse factions -- [relations] is not an object!");
			
			for (Entry<String, JsonElement> entry : obj.getAsJsonObject("relations").entrySet()) {
				
				final Faction from = result.getBy(entry.getKey());
				if (from == null)
					throw new JsonParseException("Cannot parse factions -- [relations] contains [" + entry.getKey()
							+ "] which has not been described in [factions]!");
				
				if (!entry.getValue().isJsonObject())
					throw new JsonParseException(
							"Cannot parse factions -- [relations].[" + entry.getKey() + "] is not an object!");
				
				final JsonObject relation = entry.getValue().getAsJsonObject();
				for (Entry<String, JsonElement> relationEntry : relation.entrySet()) {
					
					final Faction to = result.getBy(relationEntry.getKey());
					if (to == null)
						throw new JsonParseException(
								"Cannot parse factions -- [relations].[" + entry.getKey() + "] contains ["
										+ relationEntry.getKey() + "] which has not been described in [factions]!");
					
					final float relationValue = relationEntry.getValue().getAsFloat();
					
					result.updateRelation(from, to, relationValue);
				}
			}
			
			return result;
		}
		
	}
}
