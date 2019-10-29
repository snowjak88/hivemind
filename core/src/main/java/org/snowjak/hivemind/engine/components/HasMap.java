/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.map.EntityMap;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} knows about its surroundings, both via a
 * {@link GameMap} (to record the terrain) and an {@link EntityMap} (to record
 * {@link Entity}-locations).
 * 
 * @author snowjak88
 *
 */
public class HasMap implements Component, Poolable {
	
	private GameMap map = null;
	private EntityMap entities = new EntityMap();
	private transient ExtGreasedRegion updatedLocations = new ExtGreasedRegion(1, 1);
	
	public GameMap getMap() {
		
		return map;
	}
	
	public void setMap(GameMap map) {
		
		this.map = map;
	}
	
	public EntityMap getEntities() {
		
		return entities;
	}
	
	public void setEntities(EntityMap entities) {
		
		this.entities = entities;
	}
	
	public ExtGreasedRegion getUpdatedLocations() {
		
		return updatedLocations;
	}
	
	public void setUpdatedLocations(ExtGreasedRegion updatedLocations) {
		
		this.updatedLocations = updatedLocations;
	}
	
	@Override
	public void reset() {
		
		map = null;
		entities.clear();
		updatedLocations.resizeAndEmpty(0, 0);
	}
}
