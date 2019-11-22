/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import java.util.logging.Logger;

import org.snowjak.hivemind.RNG;
import org.snowjak.hivemind.engine.components.CanMove;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasPathfinder;
import org.snowjak.hivemind.util.EntitySubscription;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IntervalIteratingSystem;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Measurement;

/**
 * For any {@link Entity Entities} that {@link CanMove} and {@link HasMap},
 * ensures that the Entity's associated {@link HasPathfinder} is created and the
 * cached {@link DijkstraMap} instance kept updated with the latest-and-greatest
 * HasMap content.
 * 
 * @author snowjak88
 *
 */
public class PathfinderUpdatingSystem extends IntervalIteratingSystem {
	
	private static final Logger LOG = Logger.getLogger(PathfinderUpdatingSystem.class.getName());
	private static final float INTERVAL = 1f;
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<HasPathfinder> HAS_PATHFINDER = ComponentMapper.getFor(HasPathfinder.class);
	
	private final EntitySubscription entitiesNeedingPathfinders = new EntitySubscription(
			Family.all(CanMove.class, HasMap.class).get(),
			(e) -> e.add(getEngine().createComponent(HasPathfinder.class)), (e) -> e.remove(HasPathfinder.class));
	
	public PathfinderUpdatingSystem() {
		
		super(Family.all(CanMove.class, HasMap.class).get(), INTERVAL);
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		entitiesNeedingPathfinders.registerWith(engine);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		super.removedFromEngine(engine);
		entitiesNeedingPathfinders.unregisterWith(engine);
	}
	
	@Override
	public void updateInterval() {
		
		final ProfilerTimer timer = Profiler.get().start("PathfinderUpdatingSystem (overall)");
		
		super.updateInterval();
		
		timer.stop();
	}
	
	@Override
	protected void processEntity(Entity entity) {
		
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
		
		if (hasUpdatedCells || pathfinderRequiresResize) {
			if (hasPathfinder.getLock().tryLock()) {
				
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
					hasPathfinder.setPathfinder(
							new DijkstraMap(hasMap.getMap().getSquidCharMap(), Measurement.EUCLIDEAN, RNG.get()));
				
				hasPathfinder.getLock().unlock();
			}
		}
		
	}
}
