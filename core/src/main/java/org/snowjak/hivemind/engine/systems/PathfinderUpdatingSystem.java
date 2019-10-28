/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.CanMove;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasPathfinder;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidai.DijkstraMap;

/**
 * For any {@link Entity Entities} that {@link CanMove} and {@link HasMap},
 * ensures that the Entity's associated {@link HasPathfinder} is created and the
 * cached {@link DijkstraMap} instance kept updated with the latest-and-greatest
 * HasMap content.
 * 
 * @author snowjak88
 *
 */
public class PathfinderUpdatingSystem extends IteratingSystem implements EntityListener {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<HasPathfinder> HAS_PATHFINDER = ComponentMapper.getFor(HasPathfinder.class);
	
	public PathfinderUpdatingSystem() {
		
		super(Family.all(CanMove.class, HasMap.class).get());
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		engine.addEntityListener(this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		super.removedFromEngine(engine);
		engine.removeEntityListener(this);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		//
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		entity.remove(HasPathfinder.class);
	}
	
	@Override
	protected void processEntity(Entity entity, float delta) {
		
		final HasMap hasMap = HAS_MAP.get(entity);
		if (hasMap.getMap() == null)
			return;
		
		final HasPathfinder hasPathfinder;
		if (!HAS_PATHFINDER.has(entity)) {
			hasPathfinder = getEngine().createComponent(HasPathfinder.class);
			entity.add(hasPathfinder);
		} else
			hasPathfinder = HAS_PATHFINDER.get(entity);
		
		if (hasPathfinder.getLock().tryAcquire()) {
			
			if (hasPathfinder.getPathfinder() != null) {
				
				//
				// Reinitialize an existing pathfinder if width/height don't match OR if HasMap
				// has any recently-updated cells
				//
				
				final DijkstraMap pf = hasPathfinder.getPathfinder();
				if (pf.width != hasMap.getMap().getWidth() || pf.height != hasMap.getMap().getHeight()
						|| !hasMap.getUpdatedLocations().isEmpty())
					pf.initialize(hasMap.getMap().getSquidCharMap());
				
			} else
				hasPathfinder.setPathfinder(new DijkstraMap(hasMap.getMap().getSquidCharMap()));
			
			hasPathfinder.getLock().release();
		}
	}
}
