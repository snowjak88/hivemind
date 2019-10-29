/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.util.loaders.IgnoreSerialization;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Indicates that an {@link Entity} has a {@link Glyph} associated with it.
 * 
 * @author snowjak88
 *
 */
@IgnoreSerialization
public class HasGlyph implements Component, Poolable {
	
	private boolean awaitingCreation = false;
	private Glyph glyph;
	private int x, y;
	
	public boolean isAwaitingCreation() {
		
		return awaitingCreation;
	}
	
	public void setAwaitingCreation(boolean awaitingCreation) {
		
		this.awaitingCreation = awaitingCreation;
	}
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public void setGlyph(Glyph glyph) {
		
		this.glyph = glyph;
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
	
	@Override
	public void reset() {
		
		awaitingCreation = false;
		glyph = null;
		x = 0;
		y = 0;
	}
}
