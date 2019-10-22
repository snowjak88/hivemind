/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;

import com.badlogic.gdx.graphics.Color;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Changes the {@link Color} of a certain {@link Glyph}.
 * 
 * @author snowjak88
 *
 */
public class GlyphColorChangeUpdate implements GameScreenUpdate {
	
	private Glyph glyph;
	private Color newColor;
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public void setGlyph(Glyph glyph) {
		
		this.glyph = glyph;
	}
	
	public Color getNewColor() {
		
		return newColor;
	}
	
	public void setNewColor(Color newColor) {
		
		this.newColor = newColor;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		this.glyph.setColor(newColor);
	}
	
	@Override
	public void reset() {
		
		this.newColor = null;
	}
}
