/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * @author snowjak88
 *
 */
public class HasSelectedGlyph implements Component, Poolable {
	
	private Glyph glyph = null;
	private boolean isAwaitingCreation = false;
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public void setGlyph(Glyph glyph) {
		
		this.glyph = glyph;
	}
	
	public boolean isAwaitingCreation() {
		
		return isAwaitingCreation;
	}
	
	public void setAwaitingCreation(boolean isAwaitingCreation) {
		
		this.isAwaitingCreation = isAwaitingCreation;
	}
	
	@Override
	public void reset() {
		
		glyph = null;
		isAwaitingCreation = false;
	}
}
