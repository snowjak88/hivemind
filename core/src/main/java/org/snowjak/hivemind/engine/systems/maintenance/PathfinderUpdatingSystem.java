/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import org.snowjak.hivemind.engine.components.CanMove;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasPathfinder;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

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
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("PathfinderUpdatingSystem (overall)");
		
		super.update(deltaTime);
		
		timer.stop();
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
		
		final boolean hasUpdatedCells = !(hasMap.getUpdatedLocations().isEmpty());
		final boolean pathfinderRequiresResize = (hasPathfinder.getPathfinder() == null
				|| hasPathfinder.getPathfinder().width != hasMap.getMap().getWidth()
				|| hasPathfinder.getPathfinder().height != hasMap.getMap().getHeight());
		
		if (hasUpdatedCells || pathfinderRequiresResize)
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
