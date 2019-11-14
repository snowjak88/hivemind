/**
 * 
 */
package org.snowjak.hivemind.map;

import org.snowjak.hivemind.Materials;
import org.snowjak.hivemind.Materials.Material;
import org.snowjak.hivemind.TerrainTypes;
import org.snowjak.hivemind.TerrainTypes.TerrainType;
import org.snowjak.hivemind.util.ArrayUtil;
import org.snowjak.hivemind.util.ExtGreasedRegion;
import org.snowjak.hivemind.util.cache.ColorCache;

import com.badlogic.gdx.graphics.Color;

import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;

/**
 * Encapsulates knowledge about the game-map. Such knowledge may be either total
 * (e.g., in the case of the "global"/"master" instance) or partial (in the case
 * of an individual's "known" instance).
 * <p>
 * Note that GameMap is written to be thread-safe, with all appropriate methods
 * {@code synchronized}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class GameMap {
	
	private int width = 1, height = 1;
	private short[][] terrain = new short[0][0], material = new short[1][1];
	private ExtGreasedRegion known = new ExtGreasedRegion(1, 1);
	
	private double[][] visibility = new double[1][1];
	private char[][] squidCharMap = null, charMap = null;
	
	/**
	 * Construct a new (empty) GameMap of the given size.
	 * 
	 * @param width
	 * @param height
	 */
	public GameMap(int width, int height) {
		
		resize(width, height);
	}
	
	/**
	 * Construct a new GameMap, making an exact copy of an existing GameMap.
	 * <p>
	 * Note that this constructor will synchronize on {@code toCopy}.
	 * </p>
	 * 
	 * @param toCopy
	 */
	public GameMap(GameMap toCopy) {
		
		synchronized (toCopy) {
			this.width = toCopy.width;
			this.height = toCopy.height;
			this.terrain = ArrayUtil.copy(toCopy.terrain);
			this.material = ArrayUtil.copy(toCopy.material);
			this.known.remake(toCopy.known);
			
			this.visibility = ArrayUtil.copy(toCopy.visibility);
		}
	}
	
	/**
	 * Construct a new GameMap, making an exact copy of any existing GameMap, but
	 * only within the given {@link ExtGreasedRegion}.
	 * 
	 * @param toCopy
	 * @param onlyWithin
	 */
	public GameMap(GameMap toCopy, ExtGreasedRegion onlyWithin) {
		
		synchronized (toCopy) {
			this.width = toCopy.width;
			this.height = toCopy.height;
			this.terrain = new short[width][height];
			this.material = new short[width][height];
			
			ArrayUtil.fill(terrain, (short) -1);
			ArrayUtil.fill(material, (short) -1);
			
			this.known.resizeAndEmpty(width, height).or(onlyWithin);
			
			this.visibility = new double[width][height];
			onlyWithin.inverseMask(toCopy.visibility, 0);
			
			this.insert(toCopy, onlyWithin);
		}
	}
	
	/**
	 * Construct a new GameMap from the given {@code char[][]}. The configured
	 * {@link TerrainType}s are scanned, and a random TerrainType, whose character
	 * matches each given {@code char}, will be selected for each cell.
	 * 
	 * @param chars
	 * @param useSquidMappings
	 *            if {@code true}, {@code chars[][]} will be matched against
	 *            {@link TerrainType#getSquidChar()}; if {@code false}, against
	 *            {@link TerrainType#getCh()}
	 * @param materials
	 * @throws IllegalArgumentException
	 *             if the given {@code char[][]} is a jagged array
	 */
	public GameMap(char[][] chars, Material[][] materials, boolean useSquidMappings) {
		
		this.width = chars.length;
		this.height = chars[0].length;
		
		terrain = new short[width][height];
		material = new short[width][height];
		
		ArrayUtil.fill(terrain, (short) -1);
		ArrayUtil.fill(material, (short) -1);
		
		this.visibility = new double[width][height];
		
		for (int i = 0; i < chars.length; i++) {
			if (chars[i].length != height)
				throw new IllegalArgumentException("Jagged arrays are not supported as game-maps!");
			
			for (int j = 0; j < chars[i].length; j++) {
				
				final TerrainType tt = (useSquidMappings) ? TerrainTypes.get().getRandomForSquidChar(chars[i][j])
						: TerrainTypes.get().getRandomForChar(chars[i][j]);
				
				set(i, j, TerrainTypes.get().getIndexOf(tt), Materials.get().getIndex(materials[i][j]));
			}
		}
		this.known.refill(terrain, 0, Short.MAX_VALUE);
	}
	
	/**
	 * Construct a new GameMap from the given arrays and "known" region.
	 * 
	 * @param chars
	 * @param known
	 * @param useSquidMappings
	 *            if {@code true}, {@code chars[][]} will be matched against
	 *            {@link TerrainType#getSquidChar()}; if {@code false}, against
	 *            {@link TerrainType#getCh()}
	 * @param materials
	 * @throws IllegalArgumentException
	 *             if the sizes of {@code chars}, {@code foreground}, or
	 *             {@code background} do not match
	 */
	public GameMap(char[][] chars, Material[][] materials, GreasedRegion known, boolean useSquidMappings) {
		
		if (chars.length != known.width || chars[0].length != known.height)
			throw new IllegalArgumentException("Cannot create a new GameMap -- given map-elements do not match sizes.");
		
		this.width = chars.length;
		this.height = chars[0].length;
		
		terrain = new short[width][height];
		material = new short[width][height];
		visibility = new double[width][height];
		
		ArrayUtil.fill(terrain, (short) -1);
		ArrayUtil.fill(material, (short) -1);
		
		for (int i = 0; i < chars.length; i++) {
			if (chars[i].length != height)
				throw new IllegalArgumentException("Jagged arrays are not supported as game-maps!");
			
			for (int j = 0; j < chars[i].length; j++) {
				
				if (!known.contains(i, j))
					continue;
				
				final TerrainType tt = (useSquidMappings) ? TerrainTypes.get().getRandomForSquidChar(chars[i][j])
						: TerrainTypes.get().getRandomForChar(chars[i][j]);
				
				set(i, j, TerrainTypes.get().getIndexOf(tt), Materials.get().getIndex(materials[i][j]));
			}
		}
		
		this.known = new ExtGreasedRegion(known);
	}
	
	/**
	 * <p>
	 * If this GameMap is already of the given size, this method does nothing.
	 * </p>
	 * <p>
	 * If this GameMap is not already of the given size, this method will update
	 * this GameMap's size. This method will completely erase the map's contents in
	 * the process.
	 * </p>
	 * 
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		
		resize(width, height, true);
	}
	
	/**
	 * <p>
	 * If this GameMap is already of the given size, this method does nothing.
	 * </p>
	 * <p>
	 * If this GameMap is not already of the given size, this method will update
	 * this GameMap's size. This method will completely erase the map's contents in
	 * the process.
	 * </p>
	 * 
	 * 
	 * @param width
	 * @param height
	 * @param recalculate
	 */
	public void resize(int width, int height, boolean recalculate) {
		
		synchronized (this) {
			if (this.width == width && this.height == height)
				return;
			
			this.width = width;
			this.height = height;
			
			terrain = new short[width][height];
			material = new short[width][height];
			
			ArrayUtil.fill(terrain, (short) -1);
			ArrayUtil.fill(material, (short) -1);
			
			known.resizeAndEmpty(width, height);
			
			this.visibility = new double[width][height];
			
			squidCharMap = null;
			charMap = null;
		}
	}
	
	/**
	 * Insert the contents of the given GameMap into this GameMap, but only within
	 * {@code insertOnly}.
	 * <p>
	 * Note that this method not only synchronizes on this GameMap, but
	 * <em>also</em> on {@code insertFrom}. Be wary lest you introduce deadlocks!
	 * </p>
	 * <p>
	 * Note that, if {@code insertFrom} is not the same size as this GameMap, then
	 * this GameMap will be {@link #resize(int, int, boolean) resized} (and erased!)
	 * to match before the insertion takes place.
	 * </p>
	 * 
	 * @param insertFrom
	 * @param insertOnly
	 */
	public void insert(GameMap insertFrom, ExtGreasedRegion insertOnly) {
		
		synchronized (this) {
			synchronized (insertFrom) {
				if (this.width != insertFrom.width || this.height != insertFrom.height)
					resize(insertFrom.width, insertFrom.height, false);
				
				this.terrain = insertOnly.inverseMask(this.terrain, insertFrom.terrain);
				this.material = insertOnly.inverseMask(this.material, insertFrom.material);
				this.visibility = insertOnly.inverseMask(this.visibility, insertFrom.visibility);
				
				this.known.or(insertOnly);
				
				squidCharMap = null;
				charMap = null;
			}
		}
	}
	
	/**
	 * Sets this map's contents to the given values.
	 * <p>
	 * If the given location is not {@link #isInMap(int, int) on this map}, then
	 * this method does nothing.
	 * </p>
	 * 
	 * @param c
	 * @param type
	 *            {@code null} to leave unchanged
	 * @param material
	 *            {@code null} to leave unchanged
	 */
	public void set(Coord c, TerrainType type, Material material) {
		
		set(c.x, c.y, type, material);
	}
	
	/**
	 * Sets this map's contents to the given values.
	 * <p>
	 * If the given location is not {@link #isInMap(int, int) on this map}, then
	 * this method does nothing.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param type
	 *            {@code null} to leave unchanged
	 * @param material
	 *            {@code null} to leave unchanged
	 */
	public void set(int x, int y, TerrainType type, Material material) {
		
		set(x, y, TerrainTypes.get().getIndexOf(type), Materials.get().getIndex(material));
	}
	
	/**
	 * Sets this map's contents to the given values.
	 * <p>
	 * If the given location is not {@link #isInMap(int, int) on this map}, then
	 * this method does nothing.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param terrainType
	 *            {@code < 0} to leave unchanged
	 * @param material
	 *            {@code < 0} to leave unchanged
	 */
	public void set(int x, int y, short terrainType, short material) {
		
		synchronized (this) {
			if (!isInMap(x, y))
				return;
			
			if (terrainType >= 0)
				this.terrain[x][y] = terrainType;
			if (material >= 0)
				this.material[x][y] = material;
			
			this.known.set((this.terrain[x][y] >= 0), x, y);
			visibility[x][y] = TerrainTypes.get().getAt(this.terrain[x][y]).getVisibilityResistance()
					* Materials.get().get(this.material[x][y]).getVisibilityResistance();
			getSquidCharMap()[x][y] = (terrainType < 0) ? 0 : TerrainTypes.get().getAt(terrainType).getSquidChar();
			charMap = null;
		}
	}
	
	/**
	 * Compile a {@code char[][]} consisting of every map-cell's
	 * {@link TerrainType#getSquidChar()}.
	 * 
	 * @return
	 */
	public char[][] getSquidCharMap() {
		
		synchronized (this) {
			if (squidCharMap == null) {
				squidCharMap = new char[width][height];
				for (int i = 0; i < width; i++)
					for (int j = 0; j < height; j++) {
						final TerrainType tt = getTerrain(i, j);
						squidCharMap[i][j] = (tt == null) ? 0 : tt.getSquidChar();
					}
			}
			return squidCharMap;
		}
	}
	
	public short[][] getMaterialIndexMap() {
		
		synchronized (this) {
			return material;
		}
	}
	
	/**
	 * Compile a {@code double[][]} representing the visibility-resistance of the
	 * Map (being the product of the terrain-type and material
	 * visibility-resistances).
	 * 
	 * @return
	 */
	public double[][] getVisibilityResistance() {
		
		synchronized (this) {
			return visibility;
		}
	}
	
	/**
	 * Get the active {@link TerrainType} at the given location, or {@code null} if
	 * there is no assigned TerrainType or the location is outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public TerrainType getTerrain(Coord c) {
		
		return getTerrain(c.x, c.y);
	}
	
	/**
	 * Get the active {@link TerrainType} at the given location, or {@code null} if
	 * there is no assigned TerrainType or the location is outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public TerrainType getTerrain(int x, int y) {
		
		return TerrainTypes.get().getAt(getTerrainIndex(x, y));
	}
	
	/**
	 * Get the active {@link TerrainType}-index at the given location, or {@code -1}
	 * if there is no assigned TerrainType or the location is outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public short getTerrainIndex(Coord c) {
		
		return getTerrainIndex(c.x, c.y);
	}
	
	/**
	 * Get the active {@link TerrainType} at the given location, or {@code null} if
	 * there is no assigned TerrainType or the location is outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public short getTerrainIndex(int x, int y) {
		
		synchronized (this) {
			if (!isInMap(x, y))
				return -1;
			return terrain[x][y];
		}
	}
	
	/**
	 * Get the active {@link Material} at the given location, or {@code null} if
	 * there is no assigned Material or the location is outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public Material getMaterial(Coord c) {
		
		return getMaterial(c.x, c.y);
	}
	
	/**
	 * Get the active {@link Material} at the given location, or {@code null} if
	 * there is no assigned Material or the location is outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Material getMaterial(int x, int y) {
		
		return Materials.get().get(getMaterialIndex(x, y));
	}
	
	/**
	 * Get the active {@link Material}-index at the given location, or {@code -1} if
	 * there is no assigned Material or the location is outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public short getMaterialIndex(Coord c) {
		
		return getMaterialIndex(c.x, c.y);
	}
	
	/**
	 * Get the active {@link Material} at the given location, or {@code null} if
	 * there is no assigned Material or the location is outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public short getMaterialIndex(int x, int y) {
		
		synchronized (this) {
			if (!isInMap(x, y))
				return -1;
			return material[x][y];
		}
	}
	
	/**
	 * Get the character at the given location, or {@code 0} if the given location
	 * is either unknown or outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public char getChar(Coord c) {
		
		if (c == null)
			return 0;
		return getChar(c.x, c.y);
	}
	
	/**
	 * Get the character at the given location, or {@code 0} if the given location
	 * is either unknown or outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public char getChar(int x, int y) {
		
		synchronized (this) {
			final TerrainType tt = getTerrain(x, y);
			if (tt == null)
				return 0;
			return tt.getCh();
		}
	}
	
	/**
	 * Get the foreground {@link Color} at the given location, or {@code null} if
	 * the given location is either unknown or outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public Color getForeground(Coord c) {
		
		if (c == null)
			return null;
		return getForeground(c.x, c.y);
	}
	
	/**
	 * Get the foreground {@link Color} at the given location, or {@code null} if
	 * the given location is either unknown or outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Color getForeground(int x, int y) {
		
		synchronized (this) {
			
			final TerrainType tt = getTerrain(x, y);
			if (tt == null)
				return null;
			
			return tt.getForeground();
		}
	}
	
	/**
	 * Get the background {@link Color} at the given location, or {@code null} if
	 * the given location is either unknown or outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public Color getBackground(Coord c) {
		
		if (c == null)
			return null;
		return getBackground(c.x, c.y);
	}
	
	/**
	 * Get the background {@link Color} at the given location, or {@code null} if
	 * the given location is either unknown or outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Color getBackground(int x, int y) {
		
		synchronized (this) {
			final Material mat = getMaterial(x, y);
			if (mat == null)
				return null;
			
			return mat.getColor();
		}
	}
	
	/**
	 * Determine if the given map-location is "known".
	 * 
	 * @param c
	 * @return
	 */
	public boolean isKnown(Coord c) {
		
		if (c == null)
			return false;
		return isKnown(c.x, c.y);
	}
	
	/**
	 * Determine if the given map-location is "known".
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isKnown(int x, int y) {
		
		synchronized (this) {
			return known.contains(x, y);
		}
	}
	
	/**
	 * Get the "known" region underlying this map.
	 * 
	 * @return
	 */
	public ExtGreasedRegion getKnown() {
		
		return known;
	}
	
	/**
	 * Determine if the given map-location is actually located on this map.
	 * 
	 * @param c
	 * @return {@code false} if {@code c} is {@code null} or falls outside this map
	 * @see #isInMap(int, int)
	 */
	public boolean isInMap(Coord c) {
		
		if (c == null)
			return false;
		return isInMap(c.x, c.y);
	}
	
	/**
	 * Determine if the given map-location is actually located on this map -- i.e.,
	 * does it fall within <code> [0,0]-[{@link #getWidth() width}-1,{@link {@link
	 * #getHeight() height}-1]}</code>
	 * 
	 * @param x
	 * @param y
	 * 
	 * @return
	 */
	public boolean isInMap(int x, int y) {
		
		synchronized (this) {
			return (x >= 0 && y >= 0 && x < width && y < height);
		}
	}
	
	/**
	 * Clear this map -- i.e., erase its contents.
	 */
	public void clear() {
		
		synchronized (this) {
			ArrayUtil.fill(terrain, (short) -1);
			ArrayUtil.fill(material, (short) -1);
			known.clear();
			ArrayUtil.fill(visibility, 0d);
			squidCharMap = null;
			charMap = null;
		}
	}
	
	/**
	 * Erase this map, but only within the given {@link ExtGreasedRegion}.
	 * 
	 * @param onlyWithin
	 */
	public void clear(ExtGreasedRegion onlyWithin) {
		
		synchronized (this) {
			terrain = onlyWithin.inverseMask(terrain, (short) -1);
			material = onlyWithin.inverseMask(material, (short) -1);
			known.andNot(onlyWithin);
			visibility = onlyWithin.inverseMask(visibility, 0d);
			squidCharMap = null;
			charMap = null;
		}
	}
	
	public int getWidth() {
		
		return width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
	/**
	 * Given a 2D map of {@link Color} references, "compress" that to an array of
	 * {@code short}s -- being indices into the {@link ColorCache}.
	 * 
	 * @param colors
	 * @return
	 */
	public static short[][] compressColors(Color[][] colors) {
		
		final short[][] result = new short[colors.length][colors[0].length];
		for (int x = 0; x < colors.length; x++)
			for (int y = 0; y < colors[x].length; y++)
				result[x][y] = ColorCache.get().get(colors[x][y]);
		return result;
	}
	
	/**
	 * Given a 2D map of {@link ColorCache#get(short) color-indices}, convert that
	 * map into a 2D array of {@link Color} references.
	 * 
	 * @param colorIndices
	 * @return
	 */
	public static Color[][] uncompressColors(short[][] colorIndices) {
		
		final Color[][] result = new Color[colorIndices.length][colorIndices[0].length];
		for (int x = 0; x < colorIndices.length; x++)
			for (int y = 0; y < colorIndices[x].length; y++)
				result[x][y] = ColorCache.get().get(colorIndices[x][y]);
		return result;
	}
}
