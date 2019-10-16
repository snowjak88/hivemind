/**
 * 
 */
package org.snowjak.hivemind.ui;

import org.snowjak.hivemind.display.Fonts;

import com.badlogic.gdx.graphics.Texture;

/**
 * Encapsulates the skin used by this game's UI. Basically presents a configured
 * singleton {@link com.badlogic.gdx.scenes.scene2d.ui.Skin}.
 * <p>
 * Note that, at present, common fonts are held by {@link Fonts}, not this
 * object. This may end up changing, I don't know.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class Skin extends com.badlogic.gdx.scenes.scene2d.ui.Skin {
	
	public static final String BUTTON_UP = "button-up", BUTTON_DOWN = "button-down", BUTTON_CHECKED = "button-checked";
	
	private static Skin __INSTANCE = null;
	
	/**
	 * @return the singleton {@link Skin} instance
	 */
	public static Skin get() {
		
		if (__INSTANCE == null)
			synchronized (Skin.class) {
				if (__INSTANCE == null)
					__INSTANCE = new Skin();
			}
		return __INSTANCE;
	}
	
	private Skin() {
		
		super();
		
		this.add(BUTTON_UP, new Texture("data/button-background-up.png"));
		this.add(BUTTON_DOWN, new Texture("data/button-background-down.png"));
		this.add(BUTTON_CHECKED, new Texture("data/button-background-checked.png"));
	}
}
