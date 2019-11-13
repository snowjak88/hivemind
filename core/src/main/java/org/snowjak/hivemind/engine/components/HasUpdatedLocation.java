/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.engine.systems.maintenance.UpdatedLocationResettingSystem;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.Coord;

/**
 * Indicates that an {@link Entity} has changed its {@link HasLocation
 * location}. This component will be retained by a Entity for 1 tick, and then
 * automatically removed (by the {@link UpdatedLocationResettingSystem}).
 * 
 * @author snowjak88
 *
 */
public class HasUpdatedLocation implements Component, Poolable {
	
	private int tickCounter = 0;
	private Coord oldLocation = null, newLocation = null;
	
	public void incrementTickCounter() {
		
		tickCounter++;
	}
	
	public int getTickCounter() {
		
		return tickCounter;
	}
	
	public Coord getOldLocation() {
		
		return oldLocation;
	}
	
	public void setOldLocation(Coord oldLocation) {
		
		this.oldLocation = oldLocation;
	}
	
	public Coord getNewLocation() {
		
		return newLocation;
	}
	
	public void setNewLocation(Coord newLocation) {
		
		this.newLocation = newLocation;
	}
	
	@Override
	public void reset() {
		
		this.tickCounter = 0;
		this.oldLocation = null;
		this.newLocation = null;
	}
}
