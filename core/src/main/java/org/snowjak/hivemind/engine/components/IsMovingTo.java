/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.Coord;

/**
 * Indicates that an {@link Entity} is moving to a map-location.
 * 
 * @author snowjak88
 *
 */
public class IsMovingTo implements Component, Poolable {
	
	private Coord destination;
	
	public Coord getDestination() {
		
		return destination;
	}
	
	public void setDestination(Coord destination) {
		
		this.destination = destination;
	}
	
	@Override
	public void reset() {
		
		destination = null;
	}
}
