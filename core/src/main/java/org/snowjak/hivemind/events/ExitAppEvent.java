/**
 * 
 */
package org.snowjak.hivemind.events;

/**
 * Indicates that the application should exit.
 * 
 * @author snowjak88
 *
 */
public class ExitAppEvent implements Event {
	
	@Override
	public void reset() {
		
		// Nothing required to reset an instance.
	}
	
}
