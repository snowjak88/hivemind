/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;

import squidpony.squidmath.Coord;

/**
 * Updates this GameScreen's center to be at the given map-location.
 * 
 * @author snowjak88
 *
 */
public class CenterScreenScrollAt implements GameScreenUpdate {
	
	private Coord location = null;
	
	public Coord getLocation() {
		
		return location;
	}
	
	public void setLocation(Coord location) {
		
		this.location = location;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		if (location == null)
			return;
		
		gameScreen.setActiveScrollTo(location);
		
	}
	
	@Override
	public void reset() {
		
		location = null;
	}
}
