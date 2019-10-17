/**
 * 
 */
package org.snowjak.hivemind.display;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.snowjak.hivemind.config.Config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import squidpony.squidgrid.gui.gdx.DefaultResources;

/**
 * Encapsulates font configuration.
 * <p>
 * Acceptable configuration values are as follows:
 * 
 * <pre>
 * <em>in properties-file</em>
 * &lt;font-name&gt;=&lt;font-descriptor&gt;
 * 
 * &lt;font-name&gt; := {@link #FONT_MAP} | {@link #FONT_HEADING} | {@link #FONT_NORMAL}
 * 
 * &lt;font-descriptor&gt; := &lt;font-file-descriptor&gt; | &lt;prefab-font&gt;
 * 
 * &lt;font-file-descriptor&gt; := file( &lt;font-file&gt; {, &lt;PNG-file&gt; } )
 * 
 * &lt;prefab-font&gt; := {@link PrefabBitmapFonts}
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class Fonts {
	
	public static final String FONT_MAP = "fonts.map", FONT_HEADING = "fonts.heading", FONT_NORMAL = "fonts.normal";
	{
		Config.get().register(FONT_MAP, "Font used to draw the game-world", "SQUARE", true, true);
		Config.get().register(FONT_HEADING, "Font used to draw UI header-text", "LARGE", true, true);
		Config.get().register(FONT_NORMAL, "Font used to draw normal UI text", "SMOOTH", true, true);
	}
	
	private static Fonts __INSTANCE = null;
	
	/**
	 * @return the singleton {@link Fonts} instance
	 */
	public static Fonts get() {
		
		if (__INSTANCE == null)
			synchronized (Fonts.class) {
				if (__INSTANCE == null)
					__INSTANCE = new Fonts();
			}
		return __INSTANCE;
	}
	
	private final Map<String, String> namesToDescriptors = new LinkedHashMap<>();
	private final Map<String, BitmapFont> namesToInstances = new LinkedHashMap<>();
	
	private Fonts() {
		
		namesToDescriptors.put(FONT_HEADING, Config.get().get(FONT_HEADING).trim().toLowerCase());
		namesToDescriptors.put(FONT_NORMAL, Config.get().get(FONT_NORMAL).trim().toLowerCase());
		namesToDescriptors.put(FONT_MAP, Config.get().get(FONT_MAP).trim().toLowerCase());
		
		for (String key : namesToDescriptors.keySet())
			namesToInstances.put(key, parseDescriptor(namesToDescriptors.get(key)));
	}
	
	/**
	 * Get the BitmapFont associated with the defined key (e.g., {@link #FONT_MAP}).
	 * 
	 * @param key
	 * @return
	 */
	public BitmapFont get(String key) {
		
		return namesToInstances.get(key);
	}
	
	private BitmapFont parseDescriptor(String descriptor) {
		
		String working = descriptor.trim().toLowerCase();
		
		if (working.equals("file")) {
			working = working.replace("file", "");
			if (!working.startsWith("(") || !working.endsWith(")"))
				throw new IllegalArgumentException(
						"Cannot parse font descriptor: malformed 'file' descriptor: \"" + descriptor + "\"");
			
			working = working.substring(1, working.length() - 1);
			
			final String[] parts = working.split(",");
			if (parts.length < 1 || parts.length > 2)
				throw new IllegalArgumentException(
						"Cannot parse font descriptor: malformed 'file' descriptor: \"" + descriptor + "\"");
			
			if (Gdx.files == null)
				throw new IllegalStateException("Cannot parse font descriptor: LibGDX has not been initialized!");
			
			if (parts.length == 1)
				return new BitmapFont(Gdx.files.local(parts[0]));
			else
				return new BitmapFont(Gdx.files.local(parts[0]), Gdx.files.local(parts[1]), false);
		}
		
		for (PrefabBitmapFonts p : PrefabBitmapFonts.values())
			if (p.name().equalsIgnoreCase(working))
				return p.getBitmapFont();
			
		throw new IllegalArgumentException("Cannot parse font descriptor: unknown descriptor: \"" + descriptor + "\"");
	}
	
	/**
	 * Pre-defined {@link BitmapFont}s which can be referenced by name.
	 * 
	 * @author snowjak88
	 *
	 */
	public enum PrefabBitmapFonts {
		
		/**
		 * Mapped to {@link DefaultResources#getDefaultFont()}
		 */
		DEFAULT(DefaultResources::getDefaultFont),
		/**
		 * Mapped to {@link DefaultResources#getSmoothFont()}
		 */
		SMOOTH(DefaultResources::getSmoothFont),
		/**
		 * Mapped to {@link DefaultResources#getLargeSmoothFont()}
		 */
		LARGE(DefaultResources::getLargeSmoothFont),
		/**
		 * Mapped to {@link DefaultResources#getSquareSmoothFont()}
		 */
		SQUARE(DefaultResources::getSquareSmoothFont);
		
		private final Supplier<BitmapFont> tcf;
		
		PrefabBitmapFonts(Supplier<BitmapFont> supplier) {
			
			tcf = supplier;
		}
		
		public BitmapFont getBitmapFont() {
			
			return tcf.get();
		}
	}
}
