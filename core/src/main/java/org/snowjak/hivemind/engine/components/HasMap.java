/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.ExtGreasedRegion;
import org.snowjak.hivemind.util.SpatialMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} knows about its surroundings, both via a
 * {@link GameMap} (to record the terrain) and a {@link SpatialMap} (to record
 * {@link Entity}-locations).
 * 
 * @author snowjak88
 *
 */
public class HasMap implements Component, Poolable {
	
	private GameMap map;
	private SpatialMap<Entity> entities;
	private ExtGreasedRegion updatedLocations;
	
	public GameMap getMap() {
		
		return map;
	}
	
	public void setMap(GameMap map) {
		
		this.map = map;
	}
	
	public SpatialMap<Entity> getEntities() {
		
		return entities;
	}
	
	public void setEntities(SpatialMap<Entity> entities) {
		
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
		entities = null;
	}
}
