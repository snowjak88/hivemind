/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.Coord;

/**
 * Indicates that an {@link Entity}'s {@link HasLocation location} should be
 * updated.
 * 
 * @author snowjak88
 *
 */
public class NeedsUpdatedLocation implements Component, Poolable {
	
	private Coord newLocation;
	
	public Coord getNewLocation() {
		
		return newLocation;
	}
	
	public void setNewLocation(Coord newLocation) {
		
		this.newLocation = newLocation;
	}
	
	@Override
	public void reset() {
		
		newLocation = null;
	}
}
