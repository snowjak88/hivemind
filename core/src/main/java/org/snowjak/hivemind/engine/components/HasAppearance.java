/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.util.cache.ColorCache;

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
	
	private static final float GHOST_COLOR = new SColor(SColor.AURORA_CLOUD).mul(1f, 1f, 1f, 0.25f).toFloatBits();
	
	private char ch;
	private short colorIndex;
	private short ghostedColorIndex;
	
	public char getCh() {
		
		return ch;
	}
	
	public void setCh(char ch) {
		
		this.ch = ch;
	}
	
	public Color getColor() {
		
		return ColorCache.get().get(colorIndex);
	}
	
	public Color getGhostedColor() {
		
		return ColorCache.get().get(ghostedColorIndex);
	}
	
	public short getColorIndex() {
		
		return colorIndex;
	}
	
	public void setColor(Color color) {
		
		setColorIndex(ColorCache.get().get(color));
	}
	
	public void setColorIndex(short colorIndex) {
		
		this.colorIndex = colorIndex;
		
		final Color color = ColorCache.get().get(colorIndex);
		final Color ghosted = SColor.colorFromFloat(SColor.lerpFloatColors(color.toFloatBits(), GHOST_COLOR, 0.5f));
		this.ghostedColorIndex = ColorCache.get().get(ghosted);
	}
	
	@Override
	public void reset() {
		
		ch = 0;
		colorIndex = -1;
		ghostedColorIndex = -1;
	}
}
