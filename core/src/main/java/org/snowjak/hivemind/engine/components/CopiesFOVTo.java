/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.SquidID;

/**
 * Indicates that an {@link Entity} will copy its {@link HasFOV FOV} to another
 * Entity.
 * 
 * @author snowjak88
 *
 */
public class CopiesFOVTo implements Component, Poolable {
	
	private SquidID copyTo = null;
	private int radius = 32767;
	
	public SquidID getCopyTo() {
		
		return copyTo;
	}
	
	public void setCopyTo(SquidID copyTo) {
		
		this.copyTo = copyTo;
	}
	
	public int getRadius() {
		
		return radius;
	}
	
	public void setRadius(int radius) {
		
		this.radius = radius;
	}
	
	@Override
	public void reset() {
		
		copyTo = null;
		radius = 32767;
	}
}
