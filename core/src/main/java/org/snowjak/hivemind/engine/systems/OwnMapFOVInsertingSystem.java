/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.concurrent.ParallelRunner;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.ExtGreasedRegion;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * For any {@link Entity} that {@link HasFOV has its FOV calculated} and
 * {@link HasMap has a map}, this will ensure that the Entity's map is updated
 * with the visible contents of the {@link Tags#WORLD_MAP "world-map"-tagged}
 * Entity's HasMap.
 * 
 * @author snowjak88
 *
 */
public class OwnMapFOVInsertingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	private final ParallelRunner parallel = new ParallelRunner();
	
	private UniqueTagManager utm = null;
	private HasMap worldMap = null;
	
	public OwnMapFOVInsertingSystem() {
		
		super(Family.all(HasMap.class, HasFOV.class).get());
	}
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("OwnMapFOVInsertingSystem (overall)");
		
		utm = getEngine().getSystem(UniqueTagManager.class);
		
		if (!utm.has(Tags.WORLD_MAP))
			return;
		final Entity worldMapEntity = utm.get(Tags.WORLD_MAP);
		if (!HAS_MAP.has(worldMapEntity))
			return;
		worldMap = HAS_MAP.get(worldMapEntity);
		
		if (worldMap.getMap() == null)
			return;
		
		super.update(deltaTime);
		parallel.awaitAll();
		
		utm = null;
		worldMap = null;
		
		timer.stop();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final HasMap myMap = HAS_MAP.get(entity);
		final HasFOV fov = HAS_FOV.get(entity);
		
		if (fov.getVisible() == null)
			return;
		
		final Coord[] visibleCoords = fov.getVisible().asCoords();
		
		synchronized (myMap) {
			if (myMap.getMap() == null)
				myMap.setMap(new GameMap(worldMap.getMap(), fov.getVisible()));
			else
				myMap.getMap().insert(worldMap.getMap(), fov.getVisible());
			
			if (myMap.getUpdatedLocations() == null)
				myMap.setUpdatedLocations(
						new ExtGreasedRegion(worldMap.getMap().getWidth(), worldMap.getMap().getHeight()));
			if (myMap.getUpdatedLocations().width != worldMap.getMap().getWidth()
					|| myMap.getUpdatedLocations().height != worldMap.getMap().getHeight())
				myMap.getUpdatedLocations().resizeAndEmpty(worldMap.getMap().getWidth(), worldMap.getMap().getHeight());
			
			myMap.getUpdatedLocations().or(fov.getVisible());
			
			for (int i = 0; i < visibleCoords.length; i++) {
				final OrderedSet<Entity> onWorldMap = worldMap.getEntities().getAt(visibleCoords[i]);
				for (int j = 0; j < onWorldMap.size(); j++)
					myMap.getEntities().set(visibleCoords[i], onWorldMap.getAt(j));
			}
		}
	}
}
