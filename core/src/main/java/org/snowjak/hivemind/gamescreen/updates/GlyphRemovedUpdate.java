/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Remove the given {@link Glyph} from the {@link GameScreen}.
 * 
 * @author snowjak88
 *
 */
public class GlyphRemovedUpdate implements GameScreenUpdate {
	
	private Glyph glyph;
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public void setGlyph(Glyph glyph) {
		
		this.glyph = glyph;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		gameScreen.getMapSurface().removeGlyph(glyph);
	}
	
	@Override
	public void reset() {
		
		this.glyph = null;
	}
}