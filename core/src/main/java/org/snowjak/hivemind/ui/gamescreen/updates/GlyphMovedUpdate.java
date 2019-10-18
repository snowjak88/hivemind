/**
 * 
 */
package org.snowjak.hivemind.ui.gamescreen.updates;

import org.snowjak.hivemind.ui.gamescreen.GameScreen;

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
	private int fromX, fromY, toX, toY;
	private float movementDuration;
	private Runnable afterMove;
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public void setGlyph(Glyph glyph) {
		
		this.glyph = glyph;
	}
	
	public int getFromX() {
		
		return fromX;
	}
	
	public void setFromX(int fromX) {
		
		this.fromX = fromX;
	}
	
	public int getFromY() {
		
		return fromY;
	}
	
	public void setFromY(int fromY) {
		
		this.fromY = fromY;
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
	
	public float getMovementDuration() {
		
		return movementDuration;
	}
	
	public void setMovementDuration(float movementDuration) {
		
		this.movementDuration = movementDuration;
	}
	
	public Runnable getAfterMove() {
		
		return afterMove;
	}
	
	public void setAfterMove(Runnable afterMove) {
		
		this.afterMove = afterMove;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		gameScreen.getSurface().slide(glyph, fromX, fromY, toX, toY, movementDuration, afterMove);
	}
	
	@Override
	public void reset() {
		
		this.fromX = 0;
		this.fromY = 0;
		this.toX = 0;
		this.toY = 0;
		this.movementDuration = 0;
		this.afterMove = null;
	}
}
