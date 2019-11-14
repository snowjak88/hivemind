/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

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
 * will, every frame:
 * <ul>
 * <li>Copy the "current-FOV" into "previous-FOV"</li>
 * <li>Clear the "current-FOV"</li>
 * </ul>
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
		
		final HasFOV fov = HAS_FOV.get(entity);
		
		if (fov.getPrevVisible().width != fov.getVisible().width
				|| fov.getPrevVisible().height != fov.getVisible().height)
			fov.getPrevVisible().resizeAndEmpty(fov.getVisible().width, fov.getVisible().height);
		
		fov.getPrevVisible().remake(fov.getVisible());
		fov.getVisible().clear();
		
		ArrayUtil.fill(fov.getLightLevels(), 0d);
	}
}
