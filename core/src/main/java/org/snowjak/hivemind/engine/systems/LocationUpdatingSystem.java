/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasUpdatedLocation;
import org.snowjak.hivemind.engine.components.NeedsUpdatedLocation;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

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
	
	public LocationUpdatingSystem() {
		
		super(Family.all(HasLocation.class, NeedsUpdatedLocation.class).get());
	}
	
	
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("LocationUpdatingSystem (overall)");
		
		super.update(deltaTime);
		
		timer.stop();
	}



	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		//
		
		if (HAS_UPDATED_LOC.has(entity))
			entity.remove(HasUpdatedLocation.class);
		
		//
		
		final NeedsUpdatedLocation updatedLocation = NEED_UPDATED_LOC.get(entity);
		
		final HasLocation location = ComponentMapper.getFor(HasLocation.class).get(entity);
		location.setLocation(updatedLocation.getNewLocation());
		entity.add(getEngine().createComponent(HasUpdatedLocation.class));
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		if (utm.has(Tags.WORLD_MAP)) {
			final Entity worldMapEntity = utm.get(Tags.WORLD_MAP);
			if (HAS_MAP.has(worldMapEntity)) {
				final HasMap hm = HAS_MAP.get(worldMapEntity);
				hm.getEntities().set(updatedLocation.getNewLocation(), entity);
			}
		}
	}
}
