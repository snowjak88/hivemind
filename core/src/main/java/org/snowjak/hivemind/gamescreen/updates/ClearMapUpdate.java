/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;

/**
 * Clears the visible map -- i.e., resets everything visible to "blank". Note
 * that this does not accomplish the same thing as
 * {@link RemoveAllGlyphsUpdate}.
 * 
 * @author snowjak88
 *
 */
public class ClearMapUpdate implements GameScreenUpdate {
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		if (gameScreen.getSurface() == null)
			return;
		
		gameScreen.getSurface().clear();
	}
	
	@Override
	public void reset() {
		
		// Nothing required to reset.
	}
	
}
