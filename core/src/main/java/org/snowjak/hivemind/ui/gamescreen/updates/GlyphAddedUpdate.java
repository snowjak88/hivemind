/**
 * 
 */
package org.snowjak.hivemind.ui.gamescreen.updates;

import java.util.function.Consumer;

import org.snowjak.hivemind.ui.gamescreen.GameScreen;

import com.badlogic.gdx.graphics.Color;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Create a new Glyph on-screen with the designated location and color.
 * Optionally consume that Glyph after creation.
 * 
 * @author snowjak88
 *
 */
public class GlyphAddedUpdate implements GameScreenUpdate {
	
	private char ch;
	private int x, y;
	private Color color;
	private Consumer<Glyph> consumer;
	
	public char getCh() {
		
		return ch;
	}
	
	public void setCh(char ch) {
		
		this.ch = ch;
	}
	
	public int getX() {
		
		return x;
	}
	
	public void setX(int x) {
		
		this.x = x;
	}
	
	public int getY() {
		
		return y;
	}
	
	public void setY(int y) {
		
		this.y = y;
	}
	
	public Color getColor() {
		
		return color;
	}
	
	public void setColor(Color color) {
		
		this.color = color;
	}
	
	public Consumer<Glyph> getConsumer() {
		
		return consumer;
	}
	
	public void setConsumer(Consumer<Glyph> consumer) {
		
		this.consumer = consumer;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		final Glyph g = gameScreen.getSurface().glyph(ch, color, x, y);
		if (consumer != null)
			consumer.accept(g);
	}
	
	@Override
	public void reset() {
		
		this.x = 0;
		this.y = 0;
		this.ch = 0;
		this.color = null;
		this.consumer = null;
	}
}