/**
 * 
 */
package org.snowjak.hivemind.ui.gamescreen.updates;

import org.snowjak.hivemind.ui.gamescreen.GameScreen;

/**
 * Clears the visible map -- i.e., resets everything visible to "blank".
 * 
 * @author snowjak88
 *
 */
public class ClearMapUpdate implements GameScreenUpdate {
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		gameScreen.getSurface().clear();
	}
	
	@Override
	public void reset() {
		
		// Nothing required to reset.
	}
	
}
