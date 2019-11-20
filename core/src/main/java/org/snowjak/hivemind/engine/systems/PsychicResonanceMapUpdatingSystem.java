/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.components.CanSensePsychicEnergy;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasPsychicResonance;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
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

import squidpony.squidgrid.FOV;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * For all {@link Entity Entities} that {@link CanSensePsychicEnergy}, updates
 * their respective maps of all nearby Entites that {@link HasPsychicResonance}.
 * 
 * @author snowjak88
 *
 */
public class PsychicResonanceMapUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<CanSensePsychicEnergy> CAN_SENSE = ComponentMapper
			.getFor(CanSensePsychicEnergy.class);
	private static final ComponentMapper<HasPsychicResonance> HAS_RESONANCE = ComponentMapper
			.getFor(HasPsychicResonance.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	private final EntityListener energeticEntityListener = new EntityListener() {
		
		@Override
		public void entityAdded(Entity entity) {
			
			if (entity != null)
				energeticEntities.add(entity);
		}
		
		@Override
		public void entityRemoved(Entity entity) {
			
			if (entity != null)
				energeticEntities.remove(entity);
		}
	};
	
	private final OrderedSet<Entity> energeticEntities = new OrderedSet<>();
	private double[][] psychicEnergy = null, zero = null, scratch = null;
	private ExtGreasedRegion scratchRegion = null;
	
	public PsychicResonanceMapUpdatingSystem() {
		
		super(Family.all(CanSensePsychicEnergy.class).get());
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(HasPsychicResonance.class, HasLocation.class).get(),
				energeticEntityListener);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		super.removedFromEngine(engine);
		engine.addEntityListener(energeticEntityListener);
	}
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("PsychicResonanceMapUpdatingSystem (overall)");
		
		//
		// First -- psychic-sense is limited only by range. We will not concern
		// ourselves with keeping a record of "last-known" -- so we can simply calculate
		// the true psychic-energy for the whole map, and then update each sensing
		// Entity's personal map.
		//
		final Entity worldMapEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.WORLD_MAP);
		if (worldMapEntity == null || !HAS_MAP.has(worldMapEntity))
			return;
		
		final HasMap worldMap = HAS_MAP.get(worldMapEntity);
		if (worldMap.getMap() == null || worldMap.getEntities() == null)
			return;
		
		final int width = worldMap.getMap().getWidth(), height = worldMap.getMap().getHeight();
		
		if (scratchRegion == null)
			scratchRegion = new ExtGreasedRegion(width, height);
		else if (scratchRegion.width != width || scratchRegion.height != height)
			scratchRegion.resizeAndEmpty(width, height);
		
		if (psychicEnergy == null || psychicEnergy.length != width || psychicEnergy[0].length != height) {
			psychicEnergy = new double[width][height];
			zero = new double[width][height];
			scratch = new double[width][height];
		} else
			ArrayUtil.fill(psychicEnergy, 0);
		
		for (int i = 0; i < energeticEntities.size(); i++) {
			final Entity e = energeticEntities.getAt(i);
			final Coord loc = worldMap.getEntities().getLocation(e);
			if (loc == null)
				continue;
			
			final HasPsychicResonance resonance = HAS_RESONANCE.get(e);
			
			FOV.reuseFOV(zero, scratch, loc.x, loc.y, resonance.getStrength());
			FOV.addFOVsInto(psychicEnergy, scratch);
		}
		
		super.update(deltaTime);
		
		timer.stop();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final CanSensePsychicEnergy sense = CAN_SENSE.get(entity);
		if (sense.getRange() >= 0 && (!HAS_LOCATION.has(entity) || HAS_LOCATION.get(entity).getLocation() == null))
			return;
		
		if (sense.getRange() >= 0) {
			
			//
			// Compute a range-limited psychic-sense map.
			//
			final Coord loc = HAS_LOCATION.get(entity).getLocation();
			scratchRegion.empty();
			scratchRegion.insert(loc);
			scratchRegion.expand(sense.getRange());
			
			sense.setMap(scratchRegion.mask(psychicEnergy, 0));
			
		} else {
			
			//
			// Just copy the "global" psychic-sense map into this Entity's map.
			//
			if (sense.getMap() == null || sense.getMap().length != psychicEnergy.length
					|| sense.getMap()[0].length != psychicEnergy[0].length)
				sense.setMap(new double[psychicEnergy.length][psychicEnergy[0].length]);
			
			ArrayUtil.fill(sense.getMap(), psychicEnergy);
			
		}
	}
}
