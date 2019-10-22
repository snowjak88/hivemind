/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;

import com.badlogic.gdx.graphics.Color;

/**
 * Draws a character at the given map-location. If specified, will also update
 * the foreground- and background-colors.
 * 
 * @author snowjak88
 *
 */
public class DrawMapCellUpdate implements GameScreenUpdate {
	
	private char ch;
	private int x, y;
	private Color foreground, background;
	
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
	
	public Color getForeground() {
		
		return foreground;
	}
	
	public void setForeground(Color foreground) {
		
		this.foreground = foreground;
	}
	
	public Color getBackground() {
		
		return background;
	}
	
	public void setBackground(Color background) {
		
		this.background = background;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		gameScreen.getSurface().put(x, y, ch, foreground, background);
	}
	
	@Override
	public void reset() {
		
		this.ch = 0;
		this.x = 0;
		this.y = 0;
		this.foreground = null;
		this.background = null;
	}
}
