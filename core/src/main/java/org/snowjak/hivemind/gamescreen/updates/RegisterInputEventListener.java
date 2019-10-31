/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.gamescreen.InputEventListener;

/**
 * Registers a new {@link InputEventListener}
 * 
 * @author snowjak88
 *
 */
public class RegisterInputEventListener implements GameScreenUpdate {
	
	private InputEventListener listener = null;
	
	public InputEventListener getListener() {
		
		return listener;
	}
	
	public void setListener(InputEventListener listener) {
		
		this.listener = listener;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		if (listener != null)
			gameScreen.getInputProcessor().registerInputListener(listener);
	}
	
	@Override
	public void reset() {
		
		listener = null;
	}
}
