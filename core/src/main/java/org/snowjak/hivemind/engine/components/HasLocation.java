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
	
	public Coord getLocation() {
		
		return location;
	}
	
	public void setLocation(Coord location) {
		
		this.location = location;
	}
	
	@Override
	public void reset() {
		
		location = null;
	}
}
