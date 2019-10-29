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
	
	private static final float GHOST_COLOR = SColor.AURORA_CLOUD.cpy().mul(1f, 1f, 1f, 0.25f).toFloatBits();
	
	private char ch;
	private Color color = null, ghostedColor = null;
	
	public char getCh() {
		
		return ch;
	}
	
	public void setCh(char ch) {
		
		this.ch = ch;
	}
	
	public Color getColor() {
		
		return color;
	}
	
	public Color getGhostedColor() {
		
		return ghostedColor;
	}
	
	public void setColor(Color color) {
		
		this.color = color;
		this.ghostedColor = SColor.colorFromFloat(SColor.lerpFloatColors(color.toFloatBits(), GHOST_COLOR, 0.5f));
	}
	
	@Override
	public void reset() {
		
		ch = 0;
		color = null;
		ghostedColor = null;
	}
}
