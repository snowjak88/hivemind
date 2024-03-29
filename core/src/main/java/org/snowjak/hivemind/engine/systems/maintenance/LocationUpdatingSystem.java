/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.concurrent.BatchedRunner;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasUpdatedLocation;
import org.snowjak.hivemind.engine.components.NeedsUpdatedLocation;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.util.EntitySubscription;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidmath.Coord;

/**
 * System which handles {@link Entity Entities} flagged as having updated
 * locations.
 * <ol>
 * <li>updates an Entity's {@link HasLocation} from a
 * {@link NeedsUpdatedLocation}</li>
 * <li>adds an {@link HasUpdatedLocation}</li>
 * <li>updates the {@link HasMap} associated with the {@link Tags#WORLD_MAP
 * "WORLD_MAP" tag}</li>
 * </ol>
 * 
 * @author snowjak88
 *
 */
public class LocationUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<NeedsUpdatedLocation> NEED_UPDATED_LOC = ComponentMapper
			.getFor(NeedsUpdatedLocation.class);
	private static final ComponentMapper<HasUpdatedLocation> HAS_UPDATED_LOC = ComponentMapper
			.getFor(HasUpdatedLocation.class);
	
	private final EntitySubscription entitiesWithLocations = new EntitySubscription(Family.all(HasLocation.class).get(),
			this::registerEntityOnWorldMap, null);
	
	private final BatchedRunner batched = new BatchedRunner();
	
	public LocationUpdatingSystem() {
		
		super(Family.all(HasLocation.class, NeedsUpdatedLocation.class).get());
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		entitiesWithLocations.registerWith(engine);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		entitiesWithLocations.unregisterWith(engine);
		super.removedFromEngine(engine);
	}
	
	/**
	 * When an Entity with HasLocation is added to the world, we need to ensure that
	 * this initial location is logged on the world map.
	 * 
	 * @param entity
	 */
	private void registerEntityOnWorldMap(Entity entity) {
		
		if (!ComponentMapper.getFor(HasLocation.class).has(entity))
			return;
		final HasLocation loc = ComponentMapper.getFor(HasLocation.class).get(entity);
		
		final Entity worldMapEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.WORLD_MAP);
		if (worldMapEntity == null)
			return;
		if (!ComponentMapper.getFor(HasMap.class).has(worldMapEntity))
			return;
		final HasMap worldMap = ComponentMapper.getFor(HasMap.class).get(worldMapEntity);
		
		batched.add(() -> worldMap.getEntities().set(loc.getLocation(), entity));
	}
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("LocationUpdatingSystem (overall)");
		
		batched.runUpdates();
		
		super.update(deltaTime);
		
		timer.stop();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		//
		
		if (HAS_UPDATED_LOC.has(entity))
			entity.remove(HasUpdatedLocation.class);
		
		//
		
		final NeedsUpdatedLocation needsUpdate = NEED_UPDATED_LOC.get(entity);
		final HasLocation location = ComponentMapper.getFor(HasLocation.class).get(entity);
		
		final Coord oldLocation = location.getLocation();
		final Coord newLocation = needsUpdate.getNewLocation();
		
		location.setLocation(newLocation);
		
		final HasUpdatedLocation hasUpdate = getEngine().createComponent(HasUpdatedLocation.class);
		hasUpdate.setOldLocation(oldLocation);
		hasUpdate.setNewLocation(newLocation);
		entity.add(hasUpdate);
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		if (utm.has(Tags.WORLD_MAP)) {
			final Entity worldMapEntity = utm.get(Tags.WORLD_MAP);
			if (HAS_MAP.has(worldMapEntity)) {
				final HasMap hm = HAS_MAP.get(worldMapEntity);
				hm.getEntities().set(needsUpdate.getNewLocation(), entity);
			}
		}
	}
}
