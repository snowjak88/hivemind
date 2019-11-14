/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.concurrent.ParallelRunner;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.CanSee;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.IsMaterial;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.util.ExtGreasedRegion;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidgrid.FOV;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * For all {@link Entity Entities} that {@link CanSee}, adds or updates a
 * {@link HasFOV}.
 * <p>
 * Calculates FOV for the Entity, using the visibility-resistances held in the
 * {@link HasMap} belonging to the {@link Tags#WORLD_MAP "world-map"-tagged}
 * Entity.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class FOVUpdatingSystem extends IteratingSystem {
	
	private final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	private final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	private final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private final ComponentMapper<IsMaterial> IS_MATERIAL = ComponentMapper.getFor(IsMaterial.class);
	
	private final ParallelRunner parallel = new ParallelRunner();
	
	private double[][] visibilityResistance = new double[1][1];
	
	public FOVUpdatingSystem() {
		
		super(Family.all(HasLocation.class, CanSee.class).get());
	}
	
	private UniqueTagManager utm = null;
	private Entity worldMapEntity = null;
	private HasMap worldMap = null;
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("FOVUpdatingSystem (overall)");
		
		utm = getEngine().getSystem(UniqueTagManager.class);
		if (!utm.has(Tags.WORLD_MAP))
			return;
		worldMapEntity = utm.get(Tags.WORLD_MAP);
		
		if (!HAS_MAP.has(worldMapEntity))
			return;
		worldMap = HAS_MAP.get(worldMapEntity);
		
		recalculateVisibilityResistance();
		
		super.update(deltaTime);
		
		parallel.awaitAll();
		
		timer.stop();
		
		utm = null;
		worldMapEntity = null;
		worldMap = null;
	}
	
	private void recalculateVisibilityResistance() {
		
		if (visibilityResistance.length != worldMap.getMap().getWidth()
				|| visibilityResistance[0].length != worldMap.getMap().getHeight())
			visibilityResistance = new double[worldMap.getMap().getWidth()][worldMap.getMap().getHeight()];
		
		for (int x = 0; x < worldMap.getMap().getWidth(); x++)
			for (int y = 0; y < worldMap.getMap().getHeight(); y++) {
				
				visibilityResistance[x][y] = worldMap.getMap().getVisibilityResistance()[x][y];
				
				final OrderedSet<Entity> entitiesAt = worldMap.getEntities().getAt(Coord.get(x, y));
				for (int i = 0; i < entitiesAt.size(); i++) {
					final Entity e = entitiesAt.getAt(i);
					if (!IS_MATERIAL.has(e))
						continue;
					final IsMaterial material = IS_MATERIAL.get(e);
					if (material.getMaterial() == null)
						continue;
					visibilityResistance[x][y] += material.getMaterial().getVisibilityResistance();
				}
				
			}
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final CanSee canSee = CAN_SEE.get(entity);
		if (canSee.getRadius() <= 0) {
			entity.remove(HasFOV.class);
			return;
		}
		
		final HasLocation location = HAS_LOCATION.get(entity);
		
		final HasFOV fov;
		if (HAS_FOV.has(entity))
			fov = HAS_FOV.get(entity);
		else {
			fov = getEngine().createComponent(HasFOV.class);
			entity.add(fov);
		}
		
		final int width = worldMap.getMap().getWidth(), height = worldMap.getMap().getHeight();
		
		if (fov.getLightLevels() == null || fov.getLightLevels().length != width
				|| fov.getLightLevels()[0].length != height)
			fov.setLightLevels(new double[width][height]);
		
		fov.setVisible(checkSize(fov.getVisible(), width, height));
		fov.setPrevVisible(checkSize(fov.getPrevVisible(), width, height));
		fov.setVisibleDelta(checkSize(fov.getVisibleDelta(), width, height));
		fov.setNoLongerVisible(checkSize(fov.getNoLongerVisible(), width, height));
		
		parallel.add(() -> {
			
			FOV.reuseFOV(visibilityResistance, fov.getLightLevels(), location.getLocation().x, location.getLocation().y,
					canSee.getRadius());
			
			fov.getVisible().refill(fov.getLightLevels(), 1e-4).not();
			
			fov.getVisibleDelta().remake(fov.getPrevVisible()).xor(fov.getVisible());
			fov.getNoLongerVisible().remake(fov.getPrevVisible()).andNot(fov.getVisible());
			
		});
	}
	
	private ExtGreasedRegion checkSize(ExtGreasedRegion region, int width, int height) {
		
		if (region == null)
			return new ExtGreasedRegion(width, height);
		if (region.width != width || region.height != height)
			region.resizeAndEmpty(width, height);
		return region;
	}
}
