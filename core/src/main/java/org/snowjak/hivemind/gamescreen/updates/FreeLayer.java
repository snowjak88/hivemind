/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;

import squidpony.squidgrid.gui.gdx.SparseTextMap;

/**
 * Indicates that the {@link GameScreen} should un-associate a
 * {@link SparseTextMap layer} from its currently-associated name, freeing it
 * for other associations down the line.
 * 
 * @author snowjak88
 *
 */
public class FreeLayer implements GameScreenUpdate {
	
	private String name = null;
	
	public String getName() {
		
		return name;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		gameScreen.unassociateNamedLayer(name);
	}
	
	@Override
	public void reset() {
		
		name = null;
	}
}
