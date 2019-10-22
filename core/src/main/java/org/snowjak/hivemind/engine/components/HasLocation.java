/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.Coord;

/**
 * Indicates that an {@link Entity} has a location in the game-world.
 * 
 * @author snowjak88
 *
 */
public class HasLocation implements Component, Poolable {
	
	private Coord location;
	private float fractionalX = 0.5f, fractionalY = 0.5f;
	
	public Coord getLocation() {
		
		return location;
	}
	
	public void setLocation(Coord location) {
		
		this.location = location;
	}
	
	public float getFractionalX() {
		
		return fractionalX;
	}
	
	public void setFractionalX(float fractionalX) {
		
		this.fractionalX = fractionalX;
	}
	
	public float getFractionalY() {
		
		return fractionalY;
	}
	
	public void setFractionalY(float fractionalY) {
		
		this.fractionalY = fractionalY;
	}
	
	@Override
	public void reset() {
		
		location = null;
		fractionalX = 0.5f;
		fractionalY = 0.5f;
	}
}
