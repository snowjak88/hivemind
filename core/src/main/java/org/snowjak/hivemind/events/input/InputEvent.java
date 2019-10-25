/**
 * 
 */
package org.snowjak.hivemind.events.input;

import org.snowjak.hivemind.events.Event;

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.utils.Bits;

import squidpony.squidmath.Coord;

/**
 * 
 * @author snowjak88
 *
 */
public class InputEvent implements Event {
	
	private Coord screenCursor;
	private Coord mapCursor;
	private MouseButton button;
	private Bits keys;
	
	public Coord getScreenCursor() {
		
		return screenCursor;
	}
	
	public void setScreenCursor(Coord screenCursor) {
		
		this.screenCursor = screenCursor;
	}
	
	public Coord getMapCursor() {
		
		return mapCursor;
	}
	
	public void setMapCursor(Coord mapCursor) {
		
		this.mapCursor = mapCursor;
	}
	
	public MouseButton getButton() {
		
		return button;
	}
	
	public void setButton(MouseButton button) {
		
		this.button = button;
	}
	
	public Bits getKeys() {
		
		return keys;
	}
	
	public void setKeys(Bits keys) {
		
		this.keys = keys;
	}
	
	@Override
	public void reset() {
		
		screenCursor = null;
		mapCursor = null;
		button = null;
		keys.clear();
	}
	
	public enum MouseButton {
		LEFT_BUTTON(Buttons.LEFT),
		RIGHT_BUTTON(Buttons.RIGHT),
		MIDDLE_BUTTON(Buttons.MIDDLE);
		
		private final int button;
		
		MouseButton(int button) {
			
			this.button = button;
		}
		
		public boolean matches(int button) {
			
			return (this.button == button);
		}
		
		public int getButton() {
			
			return button;
		}
		
		public static MouseButton getFor(int button) {
			
			for (int i = 0; i < MouseButton.values().length; i++)
				if (MouseButton.values()[i].matches(button))
					return MouseButton.values()[i];
			return null;
		}
	}
}
