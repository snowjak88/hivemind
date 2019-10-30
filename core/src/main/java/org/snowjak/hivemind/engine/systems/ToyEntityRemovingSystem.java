/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * Receives locations and, if an entity is located at that location, removes it
 * from existence.
 * 
 * @author snowjak88
 *
 */
public class ToyEntityRemovingSystem extends EntitySystem {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	private final BlockingQueue<Coord> requests = new LinkedBlockingQueue<>();
	
	public ToyEntityRemovingSystem() {
		
		super();
	}
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		final Entity screenMapEntity = utm.get(Tags.SCREEN_MAP);
		if (screenMapEntity == null)
			return;
		if (!HAS_MAP.has(screenMapEntity))
			return;
		
		final HasMap screenMap = HAS_MAP.get(screenMapEntity);
		if (screenMap.getMap() == null)
			return;
		
		while (!requests.isEmpty()) {
			final Coord location = requests.poll();
			if (location == null)
				continue;
			
			final OrderedSet<Entity> entitiesAt = screenMap.getEntities().getAt(location);
			if (entitiesAt.isEmpty())
				continue;
			
			final Entity chosen = entitiesAt.getAt(0);
			getEngine().removeEntity(chosen);
		}
	}
	
	public void postRequest(Coord location) {
		
		requests.offer(location);
	}
}
