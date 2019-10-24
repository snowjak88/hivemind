/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity}'s FOV can be calculated.
 * 
 * @author snowjak88
 *
 */
public class CanSee implements Component, Poolable {
	
	private int radius;
	
	public int getRadius() {
		
		return radius;
	}
	
	public void setRadius(int radius) {
		
		this.radius = radius;
	}
	
	@Override
	public void reset() {
		
		radius = 0;
	}
}
