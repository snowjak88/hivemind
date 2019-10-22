/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.ComponentMappers;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasUpdatedLocation;
import org.snowjak.hivemind.engine.components.NeedsUpdatedLocation;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * System which updates an Entity's {@link HasLocation} from a
 * {@link NeedsUpdatedLocation}, and adds an {@link HasUpdatedLocation}. Also
 * handles updating the {@link HasMap} associated with the {@link Tags#WORLD_MAP
 * "WORLD_MAP" tag}.
 * 
 * @author snowjak88
 *
 */
public class LocationUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMappers.get().get(HasMap.class);
	
	public LocationUpdatingSystem() {
		
		super(Family.all(HasLocation.class, NeedsUpdatedLocation.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final ComponentMapper<HasUpdatedLocation> hasUpdatedLocation = ComponentMappers.get()
				.get(HasUpdatedLocation.class);
		final ComponentMapper<NeedsUpdatedLocation> needsUpdatedLocation = ComponentMappers.get()
				.get(NeedsUpdatedLocation.class);
		
		//
		
		if (hasUpdatedLocation.has(entity))
			entity.remove(HasUpdatedLocation.class);
		
		//
		
		if (!needsUpdatedLocation.has(entity))
			return;
		final NeedsUpdatedLocation updatedLocation = needsUpdatedLocation.get(entity);
		
		final HasLocation location = ComponentMappers.get().get(HasLocation.class).get(entity);
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
