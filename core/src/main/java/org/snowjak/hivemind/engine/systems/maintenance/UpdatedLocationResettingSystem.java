/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasUpdatedLocation;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * At the start of each tick, this sytem will remove expired
 * {@link HasUpdatedLocation} from all associated {@link Entity Entities}.
 * 
 * @author snowjak88
 *
 */
public class UpdatedLocationResettingSystem extends IteratingSystem {
	
	public UpdatedLocationResettingSystem() {
		
		super(Family.all(HasLocation.class, HasUpdatedLocation.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final HasUpdatedLocation updatedLocation = ComponentMapper.getFor(HasUpdatedLocation.class).get(entity);
		updatedLocation.incrementTickCounter();
		if (updatedLocation.getTickCounter() > 1)
			entity.remove(HasUpdatedLocation.class);
		
	}
}
