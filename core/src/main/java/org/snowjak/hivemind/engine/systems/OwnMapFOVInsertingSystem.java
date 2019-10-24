/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.concurrent.ParallelRunner;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.map.EntityMap;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

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
	
	public OwnMapFOVInsertingSystem() {
		
		super(Family.all(HasMap.class, HasFOV.class).get());
	}
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		parallel.awaitAll();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		if (!utm.has(Tags.WORLD_MAP))
			return;
		final Entity worldMapEntity = utm.get(Tags.WORLD_MAP);
		if (!HAS_MAP.has(worldMapEntity))
			return;
		final HasMap worldMap = HAS_MAP.get(worldMapEntity);
		
		if (worldMap.getMap() == null)
			return;
		
		final HasMap myMap = HAS_MAP.get(entity);
		final HasFOV fov = HAS_FOV.get(entity);
		
		if (fov.getVisible() == null)
			return;
		
		if (myMap.getMap() == null) {
			myMap.setMap(new GameMap(worldMap.getMap(), fov.getVisible()));
			myMap.setUpdatedLocations(
					new ExtGreasedRegion(worldMap.getMap().getWidth(), worldMap.getMap().getHeight()));
		} else {
			myMap.getMap().insert(worldMap.getMap(), fov.getVisible());
			myMap.getUpdatedLocations().or(fov.getVisible());
		}
		
		final OrderedSet<Entity> worldEntities;
		if (myMap.getEntities() == null) {
			myMap.setEntities(new EntityMap());
			worldEntities = worldMap.getEntities().getValues();
		} else
			worldEntities = worldMap.getEntities().getRecentlyUpdatedEntities();
		
		for (int i = 0; i < worldEntities.size(); i++) {
			final Entity worldEntity = worldEntities.getAt(i);
			myMap.getEntities().set(worldMap.getEntities().getLocation(worldEntity), worldEntity);
		}
	}
}
