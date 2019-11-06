/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.util.ArrayUtil;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * If an {@link Entity} has a {@link HasFOV pre-calculated FOV}, this system
 * will clear out that FOV (e.g., at the beginning of a new frame).
 * 
 * @author snowjak88
 *
 */
public class FOVResettingSystem extends IteratingSystem {
	
	final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	
	public FOVResettingSystem() {
		
		super(Family.all(HasFOV.class).get());
	}
	
	
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("FOVResettingSystem (overall)");
		
		super.update(deltaTime);
		
		timer.stop();
	}



	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		HAS_FOV.get(entity).getVisible().clear();
		ArrayUtil.fill(HAS_FOV.get(entity).getLightLevels(), 0d);
	}
}
