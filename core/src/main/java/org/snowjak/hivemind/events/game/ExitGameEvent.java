/**
 * 
 */
package org.snowjak.hivemind.events.game;

import org.snowjak.hivemind.events.Event;
import org.snowjak.hivemind.gamescreen.GameScreen;

/**
 * Indicates that the user wishes to exit the {@link GameScreen} and return to
 * the main menu.
 * 
 * @author snowjak88
 *
 */
public class ExitGameEvent implements Event {
	
	@Override
	public void reset() {
		
		// Nothing required to reset.
	}
	
}
