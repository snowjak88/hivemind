/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import java.util.Iterator;

import org.snowjak.hivemind.gamescreen.GameScreen;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Remove all active {@link Glyph}s on the GameScreen.
 * 
 * @author snowjak88
 *
 */
public class RemoveAllGlyphsUpdate implements GameScreenUpdate {
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		if (gameScreen.getMapSurface() == null)
			return;
		if (gameScreen.getMapSurface().glyphs == null)
			return;
		
		final Iterator<Glyph> iterator = gameScreen.getMapSurface().glyphs.iterator();
		while (iterator.hasNext()) {
			iterator.next().clearActions();
			iterator.remove();
		}
	}
	
	@Override
	public void reset() {
		
		// Nothing required to reset.
	}
}
