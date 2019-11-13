/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.WillDissipate;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * Handles {@link Entity}-{@link WillDissipate dissipation}.
 * 
 * @author snowjak88
 *
 */
public class EntityDissipationSystem extends IteratingSystem {
	
	private static final ComponentMapper<WillDissipate> WILL_DISSIPATE = ComponentMapper.getFor(WillDissipate.class);
	
	public EntityDissipationSystem() {
		
		super(Family.all(WillDissipate.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final WillDissipate wd = WILL_DISSIPATE.get(entity);
		
		wd.setIntervalRemaining(wd.getIntervalRemaining() - deltaTime);
		
		if (wd.getIntervalRemaining() <= 0f)
			getEngine().removeEntity(entity);
	}
}
