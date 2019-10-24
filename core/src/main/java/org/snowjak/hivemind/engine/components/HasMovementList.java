/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import java.util.LinkedList;
import java.util.List;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.Coord;

/**
 * Indicates that an {@link Entity} has done some pathfinding and computed a
 * movement-list.
 * 
 * @author snowjak88
 *
 */
public class HasMovementList implements Component, Poolable {
	
	private LinkedList<Coord> movement = new LinkedList<>();
	
	/**
	 * @return the first element in this movement-list, or {@code null} if the list
	 *         is empty
	 */
	public Coord getCurrentMovement() {
		
		if (movement.isEmpty())
			return null;
		
		return movement.getFirst();
	}
	
	/**
	 * Drops the current movement and advances the movement-list such that
	 * subsequent calls to {@link #getCurrentMovement()} will return the next
	 * movement in the list.
	 */
	public void nextMovement() {
		
		if (!movement.isEmpty())
			movement.removeFirst();
	}
	
	/**
	 * Clears and resets the internal movement-list to the given list.
	 * 
	 * @param movement
	 */
	public void setMovementList(List<Coord> movement) {
		
		this.movement.clear();
		this.movement.addAll(movement);
	}
	
	/**
	 * Add the given location to the internal movement-list.
	 * 
	 * @param location
	 */
	public void addMovement(Coord location) {
		
		this.movement.add(location);
	}
	
	@Override
	public void reset() {
		
		this.movement.clear();
	}
}
