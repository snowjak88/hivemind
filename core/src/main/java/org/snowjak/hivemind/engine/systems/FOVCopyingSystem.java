/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.CopiesFOVTo;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.util.ArrayUtil;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * For any {@link Entity} that {@link CopiesFOVTo copies its FOV} to another
 * Entity, perform that copy.
 * 
 * @author snowjak88
 *
 */
public class FOVCopyingSystem extends IteratingSystem {
	
	final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	final ComponentMapper<CopiesFOVTo> COPY_FOV = ComponentMapper.getFor(CopiesFOVTo.class);
	
	public FOVCopyingSystem() {
		
		super(Family.all(CopiesFOVTo.class, HasFOV.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final EntityRefManager erm = getEngine().getSystem(EntityRefManager.class);
		
		if (COPY_FOV.get(entity).getCopyTo() == null)
			return;
		
		final Entity copyTo = erm.get(COPY_FOV.get(entity).getCopyTo());
		if (copyTo == null)
			return;
		
		final HasFOV source = HAS_FOV.get(entity);
		if (source.getVisible() == null)
			return;
		
		if (HAS_FOV.has(copyTo)) {
			final HasFOV destination = HAS_FOV.get(copyTo);
			destination.getVisible().or(source.getVisible());
			
			if (destination.getLightLevels() == null
					|| destination.getLightLevels().length != source.getLightLevels().length
					|| destination.getLightLevels()[0].length != source.getLightLevels()[0].length)
				destination
						.setLightLevels(new double[source.getLightLevels().length][source.getLightLevels()[0].length]);
			
			ArrayUtil.addInPlace(destination.getLightLevels(), source.getLightLevels());
			
		} else {
			final HasFOV destination = getEngine().createComponent(HasFOV.class);
			destination.setVisible(new ExtGreasedRegion(source.getVisible()));
			copyTo.add(destination);
		}
	}
}
