/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Move the designated {@link Glyph} from one location to another, with an
 * animation that lasts for so-many seconds. Optionally execute a
 * {@link Runnable} once this animation is complete.
 * 
 * @author snowjak88
 *
 */
public class GlyphMovedUpdate implements GameScreenUpdate {
	
	private Glyph glyph;
	private int toX, toY;
	private Runnable afterMove;
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public void setGlyph(Glyph glyph) {
		
		this.glyph = glyph;
	}
	
	public int getToX() {
		
		return toX;
	}
	
	public void setToX(int toX) {
		
		this.toX = toX;
	}
	
	public int getToY() {
		
		return toY;
	}
	
	public void setToY(int toY) {
		
		this.toY = toY;
	}
	
	public Runnable getAfterMove() {
		
		return afterMove;
	}
	
	public void setAfterMove(Runnable afterMove) {
		
		this.afterMove = afterMove;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		glyph.setPosition(gameScreen.getSurface().worldX(toX), gameScreen.getSurface().worldY(toY));
	}
	
	@Override
	public void reset() {
		
		this.toX = 0;
		this.toY = 0;
		this.afterMove = null;
	}
}
