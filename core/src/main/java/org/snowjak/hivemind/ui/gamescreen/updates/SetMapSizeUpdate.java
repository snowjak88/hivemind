/**
 * 
 */
package org.snowjak.hivemind.ui.gamescreen.updates;

import org.snowjak.hivemind.ui.gamescreen.GameScreen;

/**
 * Clears and, if necessary, resizes the game-map.
 * 
 * @author snowjak88
 *
 */
public class SetMapSizeUpdate implements GameScreenUpdate {
	
	private int width, height;
	
	public SetMapSizeUpdate() {
		
	}
	
	public SetMapSizeUpdate(int width, int height) {
		
		this.width = width;
		this.height = height;
	}
	
	public int getWidth() {
		
		return width;
	}
	
	public void setWidth(int width) {
		
		this.width = width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
	public void setHeight(int height) {
		
		this.height = height;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		gameScreen.resizeSurface(width, height);
	}
	
	@Override
	public void reset() {
		
		this.width = 0;
		this.height = 0;
	}
}
