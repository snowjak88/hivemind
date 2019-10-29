/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.snowjak.hivemind.RNG;
import org.snowjak.hivemind.TerrainTypes;
import org.snowjak.hivemind.TerrainTypes.TerrainType;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidmath.Coord;

/**
 * Toy system which, upon receiving a map-location, will put a wall at the given
 * map-location (taking the wall from somewhere else in the map).
 * 
 * @author snowjak88
 *
 */
public class MapScramblingSystem extends EntitySystem {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	private final BlockingQueue<Coord> requests = new LinkedBlockingQueue<>();
	
	public void postScrambleLocation(Coord location) {
		
		requests.offer(location);
	}
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		
		final Entity worldMapEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.WORLD_MAP);
		if (worldMapEntity == null)
			return;
		
		if (!HAS_MAP.has(worldMapEntity))
			return;
		final HasMap hm = HAS_MAP.get(worldMapEntity);
		if (hm.getMap() == null)
			return;
		
		final ExtGreasedRegion walls = new ExtGreasedRegion(hm.getMap().getSquidCharMap(), '#');
		
		while (!requests.isEmpty()) {
			final Coord location = requests.poll();
			if (location == null)
				continue;
			
			final TerrainType tt = hm.getMap().getTerrain(location.x, location.y);
			if (tt == null)
				continue;
			
			if (tt.getSquidChar() == '#')
				continue;
			
			if (!hm.getEntities().getAt(location).isEmpty())
				continue;
			
			Coord pickLocation = null;
			TerrainType pickTT = null;
			for (int i = 0; i < 10; i++) {
				pickLocation = walls.singleRandom(RNG.get());
				pickTT = hm.getMap().getTerrain(pickLocation);
				if (hm.getEntities().getAt(pickLocation).isEmpty() && pickTT.getSquidChar() != '#' && pickLocation.x > 0
						&& pickLocation.x < walls.width - 1 && pickLocation.y > 0 && pickLocation.y < walls.height - 1)
					break;
			}
			
			if (!hm.getEntities().getAt(pickLocation).isEmpty() || pickLocation == null
					|| pickTT.getSquidChar() == '#') {
				
				hm.getMap().set(location, TerrainTypes.get().getRandomForSquidChar('#'), null);
				continue;
			}
			
			synchronized (hm.getMap()) {
				hm.getMap().set(location, pickTT, null);
				hm.getMap().set(pickLocation, tt, null);
			}
		}
	}
}
