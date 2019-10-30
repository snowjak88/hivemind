/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.CopiesFOVTo;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.util.ArrayUtil;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * For any {@link Entity} that {@link CopiesFOVTo copies its FOV} to another
 * Entity, perform that copy.
 * <p>
 * Also listens for {@link CopiesFOVTo}-removals, and causes the
 * destination-Entity's HasMap (if any) to have its affected cells marked as
 * "updated".
 * </p>
 * 
 * @author snowjak88
 *
 */
public class FOVCopyingSystem extends IteratingSystem implements EntityListener {
	
	private final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	private final ComponentMapper<CopiesFOVTo> COPY_FOV = ComponentMapper.getFor(CopiesFOVTo.class);
	private final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	public FOVCopyingSystem() {
		
		super(Family.all(CopiesFOVTo.class, HasFOV.class).get());
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(CopiesFOVTo.class, HasFOV.class).get(), this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener(this);
		super.removedFromEngine(engine);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		// Nothing to do.
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		final HasFOV fov = HAS_FOV.get(entity);
		final CopiesFOVTo copyTo = COPY_FOV.get(entity);
		final EntityRefManager erm = getEngine().getSystem(EntityRefManager.class);
		
		if (!erm.has(copyTo.getCopyTo()))
			return;
		final Entity copyToEntity = erm.get(copyTo.getCopyTo());
		if (!HAS_MAP.has(copyToEntity))
			return;
		
		final HasMap copyToMap = HAS_MAP.get(copyToEntity);
		copyToMap.getUpdatedLocations().or(fov.getVisible());
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
			
			if (destination.getLightLevels().length != source.getLightLevels().length
					|| destination.getLightLevels()[0].length != source.getLightLevels()[0].length)
				destination
						.setLightLevels(new double[source.getLightLevels().length][source.getLightLevels()[0].length]);
			
			ArrayUtil.addInPlace(destination.getLightLevels(), source.getLightLevels());
			
		} else {
			final HasFOV destination = getEngine().createComponent(HasFOV.class);
			destination.setVisible(new ExtGreasedRegion(source.getVisible()));
			destination.setLightLevels(source.getLightLevels());
			copyTo.add(destination);
		}
	}
}
