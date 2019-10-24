/**
 * 
 */
package org.snowjak.hivemind.map;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import org.eclipse.collections.api.map.primitive.MutableCharObjectMap;
import org.eclipse.collections.api.map.primitive.MutableObjectShortMap;
import org.eclipse.collections.impl.map.mutable.primitive.CharObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectShortHashMap;
import org.snowjak.hivemind.RNG;
import org.snowjak.hivemind.json.Json;
import org.snowjak.hivemind.util.loaders.Loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.annotations.SerializedName;

import squidpony.squidmath.OrderedSet;

/**
 * Repository for terrain-types.
 * 
 * @author snowjak88
 *
 */
public class TerrainTypes {
	
	private static final Logger LOG = Logger.getLogger(TerrainTypes.class.getName());
	private static final String FILE_NAME = "data/terrain-types.json";
	
	private static TerrainTypes __INSTANCE = null;
	
	public static TerrainTypes get() {
		
		if (__INSTANCE == null) {
			if (Gdx.files == null)
				throw new IllegalStateException("Cannot load available terrain-types -- LibGDX not yet initialized!");
			final File terrainTypesFile = Gdx.files.local(FILE_NAME).file();
			try (FileReader fr = new FileReader(terrainTypesFile)) {
				
				__INSTANCE = Json.get().fromJson(fr, TerrainTypes.class);
				
			} catch (IOException e) {
				LOG.severe("Cannot read terrain-types file -- " + e.getClass().getSimpleName() + ": " + e.getMessage());
				throw new RuntimeException(e);
			}
		}
		
		return __INSTANCE;
	}
	
	private final MutableCharObjectMap<OrderedSet<TerrainType>> charToType = new CharObjectHashMap<>();
	private final MutableCharObjectMap<OrderedSet<TerrainType>> squidCharToType = new CharObjectHashMap<>();
	private final OrderedSet<TerrainType> allTypes = new OrderedSet<>();
	private final MutableObjectShortMap<TerrainType> typesToIndices = new ObjectShortHashMap<>();
	
	private TerrainTypes() {
		
	}
	
	/**
	 * Get all {@link TerrainType}s which are capable of replacing the given
	 * {@code char} -- i.e., whose {@link TerrainType#getSquidChar()} matches
	 * {@code ch}
	 * 
	 * @param ch
	 * @return any matching {@link TerrainType}s, or the empty set if none
	 */
	public OrderedSet<TerrainType> getForSquidChar(char ch) {
		
		synchronized (this) {
			return squidCharToType.getIfAbsentPut(ch, new OrderedSet<>());
		}
	}
	
	/**
	 * Get all {@link TerrainType}s which are capable of replacing the given
	 * {@code char} -- i.e., whose {@link TerrainType#getCh()} matches {@code ch}.
	 * 
	 * @param ch
	 * @return
	 */
	public OrderedSet<TerrainType> getForChar(char ch) {
		
		synchronized (this) {
			return charToType.getIfAbsentPut(ch, new OrderedSet<>());
		}
	}
	
	/**
	 * Get a random {@link TerrainType} that is capable of replacing the given
	 * Squid-char. If no TerrainType matches, returns null.
	 * 
	 * @param ch
	 * @return
	 * @see #getForSquidChar(char)
	 */
	public TerrainType getRandomForSquidChar(char ch) {
		
		synchronized (this) {
			
			final OrderedSet<TerrainType> availableTypes = getForSquidChar(ch);
			if (availableTypes.isEmpty())
				return null;
			
			return availableTypes.getAt(RNG.get().nextInt(availableTypes.size()));
		}
	}
	
	/**
	 * Get a random {@link TerrainType} that is capable of replacing the given char.
	 * If no TerrainType matches, returns null.
	 * 
	 * @param ch
	 * @return
	 * @see #getForChar(char)
	 */
	public TerrainType getRandomForChar(char ch) {
		
		synchronized (this) {
			
			final OrderedSet<TerrainType> availableTypes = getForChar(ch);
			if (availableTypes.isEmpty())
				return null;
			
			return availableTypes.getAt(RNG.get().nextInt(availableTypes.size()));
		}
	}
	
	/**
	 * Get all registered {@link TerrainType}s.
	 * 
	 * @return
	 */
	public OrderedSet<TerrainType> getAll() {
		
		return allTypes;
	}
	
	public void register(TerrainType type) {
		
		if (type == null)
			throw new NullPointerException("Cannot register a {null} TerrainType!");
		
		synchronized (this) {
			allTypes.add(type);
			final short newIndex = (short) allTypes.indexOf(type);
			
			charToType.getIfAbsentPut(type.ch, new OrderedSet<>()).add(type);
			squidCharToType.getIfAbsentPut(type.squidChar, new OrderedSet<>()).add(type);
			typesToIndices.put(type, newIndex);
		}
	}
	
	/**
	 * Get the index corresponding to the given {@link TerrainType}. If this
	 * TerrainType has not yet been registered, this will also automatically
	 * register that type.
	 * 
	 * @param type
	 * @return -1, if {@code type == null}
	 */
	public short getIndexOf(TerrainType type) {
		
		if (type == null)
			return -1;
		
		synchronized (this) {
			if (!typesToIndices.containsKey(type))
				register(type);
			
			return typesToIndices.get(type);
		}
	}
	
	/**
	 * Get the {@link TerrainType} corresponding to the given index.
	 * 
	 * @param index
	 * @return {@code null} if no such TerrainType has been registered
	 */
	public TerrainType getAt(short index) {
		
		synchronized (this) {
			return allTypes.getAt(index);
		}
	}
	
	public static class TerrainType {
		
		private String name;
		@SerializedName("char")
		private char ch;
		@SerializedName("squid-char")
		private char squidChar;
		private Color foreground;
		private Color background;
		private boolean navigable = true;
		@SerializedName("visibility-resistance")
		private double visibilityResistance = 0f;
		private float slipperiness = 0f;
		
		public String getName() {
			
			return name;
		}
		
		public void setName(String name) {
			
			this.name = name;
		}
		
		public char getCh() {
			
			return ch;
		}
		
		public void setCh(char ch) {
			
			this.ch = ch;
		}
		
		public char getSquidChar() {
			
			return squidChar;
		}
		
		public void setSquidChar(char squidChar) {
			
			this.squidChar = squidChar;
		}
		
		public Color getForeground() {
			
			return foreground;
		}
		
		public void setForeground(Color foreground) {
			
			this.foreground = foreground;
		}
		
		public Color getBackground() {
			
			return background;
		}
		
		public void setBackground(Color background) {
			
			this.background = background;
		}
		
		public boolean isNavigable() {
			
			return navigable;
		}
		
		public void setNavigable(boolean navigable) {
			
			this.navigable = navigable;
		}
		
		public double getVisibilityResistance() {
			
			return visibilityResistance;
		}
		
		public void setVisibilityResistance(double visibilityResistance) {
			
			this.visibilityResistance = visibilityResistance;
		}
		
		public float getSlipperiness() {
			
			return slipperiness;
		}
		
		public void setSlipperiness(float slipperiness) {
			
			this.slipperiness = slipperiness;
		}
	}
	
	public static class TerrainTypesLoader implements Loader<TerrainTypes> {
		
		@Override
		public JsonElement serialize(TerrainTypes src, Type typeOfSrc, JsonSerializationContext context) {
			
			if (src == null)
				return JsonNull.INSTANCE;
			
			final JsonArray obj = new JsonArray();
			
			for (TerrainType type : src.allTypes)
				obj.add(context.serialize(type));
			
			return obj;
		}
		
		@Override
		public TerrainTypes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (json.isJsonNull())
				return null;
			
			if (!json.isJsonArray())
				throw new JsonParseException("Cannot parse terrain-types -- expecting an array!");
			
			final TerrainTypes result = new TerrainTypes();
			
			json.getAsJsonArray().forEach(e -> result.register(context.deserialize(e, TerrainType.class)));
			
			return result;
		}
		
	}
}
