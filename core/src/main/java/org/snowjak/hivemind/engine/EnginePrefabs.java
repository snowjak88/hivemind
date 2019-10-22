/**
 * 
 */
package org.snowjak.hivemind.engine;

import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.UniqueTagManager;
import org.snowjak.hivemind.map.EntityMap;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.Entity;

import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.Coord;

/**
 * Holds entity-world prefabs in code.
 * 
 * @author snowjak88
 *
 */
public class EnginePrefabs {
	
	public static void loadTest() {
		
		final int width = 64, height = 64;
		
		final Engine eng = Engine.get();
		eng.clear();
		
		final Entity mapE = eng.createEntity();
		
		final HasMap hm = eng.createComponent(HasMap.class);
		hm.setUpdatedLocations(new ExtGreasedRegion(width, height));
		hm.setMap(new GameMap(width, height));
		hm.setEntities(new EntityMap());
		
		for (int x = 0; x < height; x++) {
			hm.getMap().set(x, 0, '#', SColor.WHITE, null);
			hm.getUpdatedLocations().insert(x, 0);
			
			hm.getMap().set(x, width - 1, '#', SColor.WHITE, null);
			hm.getUpdatedLocations().insert(x, width - 1);
		}
		
		for (int y = 0; y < width; y++) {
			hm.getMap().set(0, y, '#', SColor.WHITE, null);
			hm.getUpdatedLocations().insert(0, y);
			
			hm.getMap().set(height - 1, y, '#', SColor.WHITE, null);
			hm.getUpdatedLocations().insert(height - 1, y);
		}
		
		mapE.add(hm);
		
		eng.getSystem(UniqueTagManager.class).set(Tags.SCREEN_MAP, mapE);
		eng.addEntity(mapE);
		
		final Entity anEntity = eng.createEntity();
		
		final HasAppearance ha = eng.createComponent(HasAppearance.class);
		ha.setCh('@');
		ha.setColor(SColor.WHITE);
		anEntity.add(ha);
		
		hm.getEntities().set(Coord.get(height / 2, width / 2), anEntity);
		
		eng.addEntity(anEntity);
	}
}
