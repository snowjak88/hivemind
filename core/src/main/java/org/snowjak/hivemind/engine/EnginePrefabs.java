/**
 * 
 */
package org.snowjak.hivemind.engine;

import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.UniqueTagManager;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.Entity;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * Holds entity-world prefabs in code.
 * 
 * @author snowjak88
 *
 */
public class EnginePrefabs {
	
	public static void loadTest() {
		
		final Engine eng = Engine.get();
		eng.clear();
		
		final Entity e = eng.createEntity();
		
		final HasMap hm = eng.createComponent(HasMap.class);
		hm.setUpdatedLocations(new ExtGreasedRegion(64, 64));
		hm.setMap(new GameMap(64, 64));
		
		hm.getMap().set(32, 32, 'X', SColor.RED, null);
		hm.getUpdatedLocations().insert(32, 32);
		
		for (int x = 0; x < 64; x++) {
			hm.getMap().set(x, 0, '#', SColor.WHITE, null);
			hm.getUpdatedLocations().insert(x, 0);
			
			hm.getMap().set(x, 63, '#', SColor.WHITE, null);
			hm.getUpdatedLocations().insert(x, 63);
		}
		
		for (int y = 0; y < 64; y++) {
			hm.getMap().set(0, y, '#', SColor.WHITE, null);
			hm.getUpdatedLocations().insert(0, y);
			
			hm.getMap().set(63, y, '#', SColor.WHITE, null);
			hm.getUpdatedLocations().insert(63, y);
		}
		
		e.add(hm);
		
		eng.getSystem(UniqueTagManager.class).set(Tags.SCREEN_MAP, e);
		
		eng.addEntity(e);
	}
}
