/**
 * 
 */
package org.snowjak.hivemind.util.cache;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;

/**
 * Holds references to {@link Color}s, and handles translating them between
 * indices (i.e., {@code short}s) and Colors. The advantage of working with
 * indices are that they consume <em>much</em> less storage -- 16 bits, as
 * opposed to 32 or 64 (depending on your JVM).
 * 
 * @author snowjak88
 *
 */
public class ColorCache {
	
	private static ColorCache __INSTANCE = null;
	
	private ArrayList<Color> colors = new ArrayList<>();
	
	public static ColorCache get() {
		
		if (__INSTANCE == null)
			synchronized (ColorCache.class) {
				if (__INSTANCE == null)
					__INSTANCE = new ColorCache();
			}
		
		return __INSTANCE;
	}
	
	private ColorCache() {
		
	}
	
	/**
	 * Get the index associated with this Color. If this Color has not yet been
	 * associated, add it to the cache and get its brand-new index.
	 * 
	 * @param c
	 * @return -1 if {@code c} == {@code null}
	 */
	public short get(Color c) {
		
		if (c == null)
			return -1;
		
		synchronized (this) {
			if (colors.contains(c))
				return (short) colors.indexOf(c);
			
			colors.add(c);
			return (short) colors.indexOf(c);
		}
	}
	
	/**
	 * Get the Color associated with the given index, or {@code null} if the given
	 * index does not appear in the cache.
	 * 
	 * @param index
	 * @return
	 */
	public Color get(short index) {
		
		synchronized (this) {
			if (index < 0 || index >= colors.size())
				return null;
			
			return colors.get(index);
		}
	}
	
	/**
	 * Associate the given Color with the given index.
	 * <p>
	 * Be careful of how you assign indices. This Cache is backed by an ArrayList,
	 * and every index-location must be filled by an entry. If you specify a
	 * very-large {@code index}, this method will create a large number of
	 * {@code null} entries to reach your desired {@code index}!
	 * </p>
	 * 
	 * @param c
	 * @param index
	 */
	public void set(Color c, short index) {
		
		synchronized (this) {
			while (colors.size() <= index)
				colors.add(null);
			
			colors.set(index, c);
		}
	}
	
	/**
	 * Clear out the cache.
	 */
	public void clear() {
		
		synchronized (this) {
			colors.clear();
		}
	}
}
