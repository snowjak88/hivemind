/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.map.EntityMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;

/**
 * Handles maintenance-tasks on {@link EntityMap} instances.
 * <ul>
 * <li>Registers new {@link HasMap#getEntities() HasMap -> EntityMap} instances
 * with the Engine as {@link EntityListener}s</li>
 * <li>De-registers the same</li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
public class EntityMapMaintenanceSystem extends EntitySystem implements EntityListener {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	public EntityMapMaintenanceSystem() {
		
		super();
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(HasMap.class).get(), this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener(this);
		super.removedFromEngine(engine);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		final HasMap hm = HAS_MAP.get(entity);
		getEngine().addEntityListener(hm.getEntities());
		
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		final HasMap hm = HAS_MAP.get(entity);
		getEngine().removeEntityListener(hm.getEntities());
	}
}
