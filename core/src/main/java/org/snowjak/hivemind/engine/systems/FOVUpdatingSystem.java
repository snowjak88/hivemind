/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.snowjak.hivemind.concurrent.ParallelRunner;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.CanSee;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.IsMaterial;
import org.snowjak.hivemind.util.ArrayUtil;
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
	
	private final BlockingQueue<double[][]> scratchVisibilityResistance = new LinkedBlockingQueue<>();
	private final ParallelRunner parallel = new ParallelRunner();
	
	public FOVUpdatingSystem() {
		
		super(Family.all(HasLocation.class, CanSee.class).get());
	}
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("FOVUpdatingSystem (overall)");
		
		super.update(deltaTime);
		
		parallel.awaitAll();
		
		timer.stop();
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
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		if (!utm.has(Tags.WORLD_MAP))
			return;
		final Entity worldMapEntity = utm.get(Tags.WORLD_MAP);
		
		if (!HAS_MAP.has(worldMapEntity))
			return;
		final HasMap worldMap = HAS_MAP.get(worldMapEntity);
		
		if (fov.getLightLevels() == null || fov.getLightLevels().length != worldMap.getMap().getWidth()
				|| fov.getLightLevels()[0].length != worldMap.getMap().getHeight())
			fov.setLightLevels(new double[worldMap.getMap().getWidth()][worldMap.getMap().getHeight()]);
		
		if (fov.getVisible() == null)
			fov.setVisible(new ExtGreasedRegion(fov.getLightLevels().length, fov.getLightLevels()[0].length));
		else if (fov.getLightLevels().length != fov.getVisible().width
				|| fov.getLightLevels()[0].length != fov.getVisible().height)
			fov.getVisible().resizeAndEmpty(fov.getLightLevels().length, fov.getLightLevels()[0].length);
		
		parallel.add(() -> {
			
			//
			// We need to inspect each cell we *could* see, to gather any
			// visibility-resistances that may apply due
			// to Entities active in any of those cells.
			
			//
			// Attempt to re-use a double[][]
			double[][] visibilityResistance = scratchVisibilityResistance.poll();
			if (visibilityResistance == null || visibilityResistance.length != worldMap.getMap().getWidth()
					|| visibilityResistance[0].length != worldMap.getMap().getHeight())
				visibilityResistance = new double[worldMap.getMap().getWidth()][worldMap.getMap().getHeight()];
			else
				ArrayUtil.fill(visibilityResistance, 0);
			
			final double[][] baseVisibilityResistance = worldMap.getMap().getVisibilityResistance();
			
			ArrayUtil.addInPlace(visibilityResistance, baseVisibilityResistance);
			
			final Coord[] cellsToCheck = new ExtGreasedRegion(worldMap.getMap().getWidth(),
					worldMap.getMap().getHeight()).insert(location.getLocation()).expand8way(canSee.getRadius())
							.asCoords();
			for (int i = 0; i < cellsToCheck.length; i++) {
				
				final Coord c = cellsToCheck[i];
				
				final OrderedSet<Entity> entitiesToCheck = worldMap.getEntities().getAt(c);
				for (int j = 0; j < entitiesToCheck.size(); j++) {
					
					final Entity entityToCheck = entitiesToCheck.getAt(j);
					
					if (!IS_MATERIAL.has(entityToCheck))
						continue;
					final IsMaterial material = IS_MATERIAL.get(entityToCheck);
					if (material.getMaterial() == null)
						continue;
					
					visibilityResistance[c.x][c.y] += material.getMaterial().getVisibilityResistance()
							* material.getDepth();
				}
				
			}
			
			//
			// Now that we've calculated the terrain + entities visibility-resistances,
			// we can compute FOV.
			//
			
			FOV.reuseFOV(visibilityResistance, fov.getLightLevels(), location.getLocation().x, location.getLocation().y,
					canSee.getRadius());
			fov.getVisible().refill(fov.getLightLevels(), 1e-4).not();
			
			scratchVisibilityResistance.offer(visibilityResistance);
		});
	}
}
