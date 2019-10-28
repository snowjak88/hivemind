/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.HasBehavior;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * For each {@link Entity} that {@link HasBehavior has an associated behavior}
 * -- updates that behavior for the current frame.
 * 
 * @author snowjak88
 *
 */
public class BehaviorProcessingSystem extends IteratingSystem {
	
	final ComponentMapper<HasBehavior> HAS_BEHAVIOR = ComponentMapper.getFor(HasBehavior.class);
	
	public BehaviorProcessingSystem() {
		
		super(Family.all(HasBehavior.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final HasBehavior behavior = HAS_BEHAVIOR.get(entity);
		
		if (behavior.getBehavior() == null)
			return;
		
		behavior.getBehavior().setObject(entity);
		behavior.getBehavior().step();
	}
}