/**
 * 
 */
package org.snowjak.hivemind.map;

import org.snowjak.hivemind.util.ArrayUtil;
import org.snowjak.hivemind.util.ExtGreasedRegion;
import org.snowjak.hivemind.util.cache.ColorCache;

import com.badlogic.gdx.graphics.Color;

import squidpony.squidmath.Coord;

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
	
	private int width = 0, height = 0;
	private char[][] chars = new char[0][0];
	private short[][] foreground = new short[0][0], background = new short[0][0];
	private ExtGreasedRegion known = new ExtGreasedRegion(0, 0);
	
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
			this.chars = ArrayUtil.copy(toCopy.chars);
			this.foreground = ArrayUtil.copy(toCopy.foreground);
			this.background = ArrayUtil.copy(toCopy.background);
			this.known.remake(toCopy.known);
		}
	}
	
	/**
	 * Construct a new GameMap from the given arrays. After construction, the whole
	 * map is tagged as "known".
	 * 
	 * @param chars
	 * @param foreground
	 * @param background
	 * @throws IllegalArgumentException
	 *             if the sizes of {@code chars}, {@code foreground}, or
	 *             {@code background} do not match
	 */
	public GameMap(char[][] chars, Color[][] foreground, Color[][] background) {
		
		if (chars.length != foreground.length || chars[0].length != foreground[0].length
				|| chars.length != background.length || chars[0].length != background[0].length
				|| foreground.length != background.length || foreground[0].length != background[0].length)
			throw new IllegalArgumentException(
					"Cannot create a new GameMap -- given map-element arrays do not match sizes.");
		
		this.width = chars.length;
		this.height = chars[0].length;
		
		this.chars = ArrayUtil.copy(chars);
		this.foreground = compressColors(foreground);
		this.background = compressColors(background);
		this.known.resizeAndEmpty(width, height).fill(true);
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
		
		synchronized (this) {
			if (this.width == width && this.height == height)
				return;
			
			this.width = width;
			this.height = height;
			
			chars = new char[width][height];
			foreground = new short[width][height];
			background = new short[width][height];
			known.resizeAndEmpty(width, height);
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
	 * this GameMap will be {@link #resize(int, int) resized} (and erased!) to match
	 * before the insertion takes place.
	 * </p>
	 * 
	 * @param insertFrom
	 * @param insertOnly
	 */
	public void insert(GameMap insertFrom, ExtGreasedRegion insertOnly) {
		
		synchronized (this) {
			synchronized (insertFrom) {
				if (this.width != insertFrom.width || this.height != insertFrom.height)
					resize(insertFrom.width, insertFrom.height);
				
				this.chars = insertOnly.inverseMask(this.chars, insertFrom.chars);
				this.foreground = insertOnly.inverseMask(this.foreground, insertFrom.foreground);
				this.background = insertOnly.inverseMask(this.background, insertFrom.background);
				
				this.known.or(insertOnly);
			}
		}
	}
	
	/**
	 * Sets this map's contents to the given values.
	 * <p>
	 * If the given location is not {@link #isInMap(int, int) on this map}, then
	 * this method does nothing.
	 * </p>
	 * <p>
	 * Note that the updated map-location now counts as "known" <em>unless</em> all
	 * of the following are true:
	 * <ul>
	 * <li>{@code ch == 0}</li>
	 * <li>{@code foreground == null}</li>
	 * <li>{@code background == null}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param c
	 * @param ch
	 * @param foreground
	 * @param background
	 */
	public void set(Coord c, char ch, Color foreground, Color background) {
		
		set(c.x, c.y, ch, foreground, background);
	}
	
	/**
	 * Sets this map's contents to the given values.
	 * <p>
	 * If the given location is not {@link #isInMap(int, int) on this map}, then
	 * this method does nothing.
	 * </p>
	 * <p>
	 * Note that the updated map-location now counts as "known" <em>unless</em> all
	 * of the following are true:
	 * <ul>
	 * <li>{@code ch == 0}</li>
	 * <li>{@code foreground == null}</li>
	 * <li>{@code background == null}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param ch
	 * @param foreground
	 * @param background
	 */
	public void set(int x, int y, char ch, Color foreground, Color background) {
		
		set(x, y, ch, ColorCache.get().get(foreground), ColorCache.get().get(background));
	}
	
	/**
	 * Sets this map's contents to the given values.
	 * <p>
	 * If the given location is not {@link #isInMap(int, int) on this map}, then
	 * this method does nothing.
	 * </p>
	 * <p>
	 * Note that the updated map-location now counts as "known" <em>unless</em> all
	 * of the following are true:
	 * <ul>
	 * <li>{@code ch == 0}</li>
	 * <li>{@code foreground < 0}</li>
	 * <li>{@code background < 0}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param c
	 * @param ch
	 * @param foreground
	 * @param background
	 */
	public void set(Coord c, char ch, short foreground, short background) {
		
		set(c.x, c.y, ch, foreground, background);
	}
	
	/**
	 * Sets this map's contents to the given values.
	 * <p>
	 * If the given location is not {@link #isInMap(int, int) on this map}, then
	 * this method does nothing.
	 * </p>
	 * <p>
	 * Note that the updated map-location now counts as "known" <em>unless</em> all
	 * of the following are true:
	 * <ul>
	 * <li>{@code ch == 0}</li>
	 * <li>{@code foreground < 0}</li>
	 * <li>{@code background < 0}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param ch
	 * @param foreground
	 * @param background
	 */
	public void set(int x, int y, char ch, short foreground, short background) {
		
		synchronized (this) {
			if (!isInMap(x, y))
				return;
			
			this.chars[x][y] = ch;
			this.foreground[x][y] = foreground;
			this.background[x][y] = background;
			this.known.set((ch != 0 || foreground >= 0 || background >= 0), x, y);
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
			if (!isInMap(x, y))
				return 0;
			if (!known.contains(x, y))
				return 0;
			return chars[x][y];
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
		
		return ColorCache.get().get(getForegroundIndex(x, y));
	}
	
	/**
	 * Get the foreground color-index at the given location, or {@code -1} if the
	 * given location is either unknown or outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public short getForegroundIndex(Coord c) {
		
		if (c == null)
			return -1;
		return getForegroundIndex(c.x, c.y);
	}
	
	/**
	 * Get the foreground color-index at the given location, or {@code -1} if the
	 * given location is either unknown or outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public short getForegroundIndex(int x, int y) {
		
		synchronized (this) {
			if (!isInMap(x, y))
				return -1;
			if (!known.contains(x, y))
				return -1;
			return foreground[x][y];
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
		
		return ColorCache.get().get(getBackgroundIndex(x, y));
	}
	
	/**
	 * Get the background color-index at the given location, or {@code -1} if the
	 * given location is either unknown or outside the map.
	 * 
	 * @param c
	 * @return
	 */
	public short getBackgroundIndex(Coord c) {
		
		if (c == null)
			return -1;
		return getBackgroundIndex(c.x, c.y);
	}
	
	/**
	 * Get the background color-index at the given location, or {@code -1} if the
	 * given location is either unknown or outside the map.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public short getBackgroundIndex(int x, int y) {
		
		synchronized (this) {
			if (!isInMap(x, y))
				return -1;
			if (!known.contains(x, y))
				return -1;
			return background[x][y];
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
			ArrayUtil.fill(chars, (char) 0);
			ArrayUtil.fill(foreground, (short) 0);
			ArrayUtil.fill(background, (short) 0);
			known.clear();
		}
	}
	
	/**
	 * Erase this map, but only within the given {@link ExtGreasedRegion}.
	 * 
	 * @param onlyWithin
	 */
	public void clear(ExtGreasedRegion onlyWithin) {
		
		synchronized (this) {
			chars = onlyWithin.inverseMask(chars, (char) 0);
			foreground = onlyWithin.inverseMask(foreground, (short) -1);
			background = onlyWithin.inverseMask(background, (short) -1);
			known.andNot(onlyWithin);
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
