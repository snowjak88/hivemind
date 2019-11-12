/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * Describes an {@link Entity}'s physical appearance.
 * 
 * @author snowjak88
 *
 */
public class HasAppearance implements Component, Poolable {
	
	public static final Color GHOST_COLOR = SColor.AURORA_CLOUD;
	public static final float GHOST_COLOR_FLOAT = GHOST_COLOR.cpy().mul(1f, 1f, 1f, 0.5f).toFloatBits();
	
	private char ch;
	private Color color = null;
	private transient Color modifiedColor = null, ghostedColor = null;
	
	public char getCh() {
		
		return ch;
	}
	
	public void setCh(char ch) {
		
		this.ch = ch;
	}
	
	public Color getColor() {
		
		return color;
	}
	
	public void setColor(Color color) {
		
		this.color = color;
	}
	
	public Color getModifiedColor() {
		
		return modifiedColor;
	}
	
	public void setModifiedColor(Color modifiedColor) {
		
		this.modifiedColor = modifiedColor;
		this.ghostedColor = ghostColor(this.modifiedColor);
	}
	
	public Color getGhostedColor() {
		
		return ghostedColor;
	}
	
	@Override
	public void reset() {
		
		ch = 0;
		color = null;
		modifiedColor = null;
		ghostedColor = null;
	}
	
	public static Color ghostColor(Color color) {
		
		if (color == null)
			return GHOST_COLOR;
		
		return SColor.colorFromFloat(SColor.lerpFloatColors(color.toFloatBits(), GHOST_COLOR_FLOAT, 0.75f));
	}
}
