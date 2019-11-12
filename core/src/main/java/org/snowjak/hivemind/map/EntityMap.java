/**
 * 
 */
package org.snowjak.hivemind.map;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.maintenance.EntityMapMaintenanceSystem;
import org.snowjak.hivemind.util.SpatialMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.PooledEngine;

import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.OrderedSet;

/**
 * Being a mapping between {@link Entity Entities} and {@link Coord locations}.
 * <p>
 * Because this holds {@link Entity}-references, and we may very well be
 * recycling Entity-instances (e.g., with a {@link PooledEngine}), we need to
 * ensure that we're discarding our Entity references just as soon as the
 * Entities they reference are removed from circulation. Therefore, this class
 * implements {@link EntityListener}. You should register each new EntityMap
 * instance with the active {@link Engine} as an EntityListener:
 * 
 * <pre>
 *    ...
 *    final EntityMap myEntities = new EntityMap();
 *    {@link Context#getEngine()}.{@link Engine#addEntityListener(EntityListener) addEntityListener(myEntities)};
 *    ...
 * </pre>
 * 
 * Note that this registration will <em>probably</em> be handled automatically
 * by {@link EntityMapMaintenanceSystem}, at least for {@link Entity Entities}
 * that become associated with {@link HasMap} instances.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class EntityMap extends SpatialMap<Entity> implements EntityListener {
	
	@Override
	public void entityAdded(Entity entity) {
		
		// Nothing to do here.
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		synchronized (this) {
			//
			// If an entity is removed from the engine, we need to discard all references to
			// it.
			hardRemove(entity);
		}
	}
	
	/**
	 * Get the set of held {@link Entity Entities) at the given {@link Coord
	 * location}, that are associated with the given {@link Component}-type.
	 * 
	 * @param region
	 * @param clazz
	 * @return
	 */
	public OrderedSet<Entity> getWithin(GreasedRegion region, Class<? extends Component> clazz) {
		
		return getWithin(region, (e) -> ComponentMapper.getFor(clazz).has(e));
	}
	
	/**
	 * Get the set of held {@link Entity Entities} at the given {@link Coord
	 * location}, that are associated with the given {@link Component}-type.
	 * 
	 * @param location
	 * @param clazz
	 * @return
	 */
	public OrderedSet<Entity> getAt(Coord location, Class<? extends Component> clazz) {
		
		return getAt(location, (e) -> ComponentMapper.getFor(clazz).has(e));
	}
}
