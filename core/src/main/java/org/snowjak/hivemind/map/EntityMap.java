/**
 * 
 */
package org.snowjak.hivemind.map;

import org.snowjak.hivemind.util.SpatialMap;

import com.badlogic.ashley.core.Entity;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * Being a mapping between {@link Entity Entities} and {@link Coord locations}.
 * 
 * @author snowjak88
 *
 */
public class EntityMap extends SpatialMap<Entity> {
	
	private OrderedSet<Entity> updatedEntities = new OrderedSet<>();
	
	@Override
	public void set(Coord location, Entity value) {
		
		synchronized (this) {
			super.set(location, value);
			updatedEntities.add(value);
		}
	}
	
	/**
	 * Get a list of {@link Entity Entities} that have been updated since the last
	 * call to {@link #resetRecentlyUpdatedEntities()}.
	 * 
	 * @return
	 */
	public OrderedSet<Entity> getRecentlyUpdatedEntities() {
		
		return updatedEntities;
	}
	
	/**
	 * Reset the list of recently-updated {@link Entity Entities}.
	 */
	public void resetRecentlyUpdatedEntities() {
		
		synchronized (this) {
			updatedEntities.clear();
		}
	}
}
