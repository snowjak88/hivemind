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
	private boolean visible;
	private Color foreground, background;
	private float foregroundFloat, backgroundFloat;
	
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
	
	public boolean isVisible() {
		
		return visible;
	}
	
	public void setVisible(boolean visible) {
		
		this.visible = visible;
	}
	
	public Color getForeground() {
		
		return foreground;
	}
	
	public void setForeground(Color foreground) {
		
		this.foreground = foreground;
		this.foregroundFloat = foreground.toFloatBits();
	}
	
	public Color getBackground() {
		
		return background;
	}
	
	public void setBackground(Color background) {
		
		this.background = background;
		this.backgroundFloat = background.toFloatBits();
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		gameScreen.getSurface().putWithReverseLight(x, y, ch, foregroundFloat, backgroundFloat,
				GameScreen.NOT_VISIBLE_DARKNESS_FLOAT, (visible) ? 0 : 1f);
	}
	
	@Override
	public void reset() {
		
		this.ch = 0;
		this.x = 0;
		this.y = 0;
		visible = false;
		this.foreground = null;
		this.foregroundFloat = 0f;
		this.background = null;
		this.backgroundFloat = 0f;
	}
}
