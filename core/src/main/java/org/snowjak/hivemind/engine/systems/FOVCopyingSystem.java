/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.CopiesFOVTo;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.manager.EntityRefManager;
import org.snowjak.hivemind.util.ArrayUtil;
import org.snowjak.hivemind.util.ExtGreasedRegion;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.SquidID;

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
	private final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
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
		
		final OrderedSet<SquidID> copiesToIDs = copyTo.getCopyTo();
		
		for (int i = 0; i < copiesToIDs.size(); i++) {
			
			final SquidID copyToID = copiesToIDs.getAt(i);
			
			if (!erm.has(copyToID))
				return;
			final Entity copyToEntity = erm.get(copyToID);
			if (!HAS_MAP.has(copyToEntity))
				return;
			
			final HasMap copyToMap = HAS_MAP.get(copyToEntity);
			copyToMap.getUpdatedLocations().or(fov.getVisible());
			
		}
	}
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("FOVCopyingSystem (overall)");
		
		super.update(deltaTime);
		
		timer.stop();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final EntityRefManager erm = getEngine().getSystem(EntityRefManager.class);
		
		final CopiesFOVTo copiesFOVTo = COPY_FOV.get(entity);
		if (copiesFOVTo.getCopyTo() == null || copiesFOVTo.getCopyTo().isEmpty())
			return;
		
		final OrderedSet<SquidID> copiesToIDs = copiesFOVTo.getCopyTo();
		for (int i = 0; i < copiesToIDs.size(); i++) {
			
			final SquidID copyToID = copiesToIDs.getAt(i);
			
			final Entity copyTo = erm.get(copyToID);
			if (copyTo == null)
				return;
			
			final HasFOV source = HAS_FOV.get(entity);
			if (source.getVisible() == null)
				return;
			
			if (HAS_LOCATION.has(entity) && HAS_LOCATION.has(copyTo)) {
				final Coord fromCoord = HAS_LOCATION.get(entity).getLocation();
				final Coord toCoord = HAS_LOCATION.get(copyTo).getLocation();
				
				if (fromCoord.distance(toCoord) > COPY_FOV.get(entity).getRadius())
					return;
			}
			
			if (HAS_FOV.has(copyTo)) {
				final HasFOV destination = HAS_FOV.get(copyTo);
				
				destination.getVisible().or(source.getVisible());
				destination.getPrevVisible().or(source.getPrevVisible());
				
				destination.getVisibleDelta().remake(destination.getVisible()).xor(destination.getPrevVisible());
				destination.getNoLongerVisible().remake(destination.getPrevVisible()).andNot(destination.getVisible());
				
				if (destination.getLightLevels().length != source.getLightLevels().length
						|| destination.getLightLevels()[0].length != source.getLightLevels()[0].length)
					destination.setLightLevels(
							new double[source.getLightLevels().length][source.getLightLevels()[0].length]);
				
				ArrayUtil.addInPlace(destination.getLightLevels(), source.getLightLevels());
				
			} else {
				
				final HasFOV destination = getEngine().createComponent(HasFOV.class);
				
				destination.setVisible(new ExtGreasedRegion(source.getVisible()));
				destination.setPrevVisible(new ExtGreasedRegion(source.getPrevVisible()));
				destination.setVisibleDelta(new ExtGreasedRegion(source.getVisibleDelta()));
				destination.setNoLongerVisible(new ExtGreasedRegion(source.getNoLongerVisible()));
				
				destination.setLightLevels(source.getLightLevels());
				
				copyTo.add(destination);
			}
		}
	}
}
