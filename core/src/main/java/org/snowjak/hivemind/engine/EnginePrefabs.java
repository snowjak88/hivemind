/**
 * 
 */
package org.snowjak.hivemind.engine;

import org.snowjak.hivemind.RNG;
import org.snowjak.hivemind.behavior.Behaviors;
import org.snowjak.hivemind.engine.components.CanMove;
import org.snowjak.hivemind.engine.components.CanSee;
import org.snowjak.hivemind.engine.components.CopiesFOVTo;
import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.HasBehavior;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.EntityRefManager;
import org.snowjak.hivemind.engine.systems.UniqueTagManager;
import org.snowjak.hivemind.map.EntityMap;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.map.TerrainTypes;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.Entity;

import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.GreasedRegion;

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
		
		final Entity worldMapEntity = eng.createEntity();
		
		final HasMap worldMap = eng.createComponent(HasMap.class);
		worldMap.setUpdatedLocations(new ExtGreasedRegion(width, height));
		worldMap.setEntities(new EntityMap());
		
		final DungeonGenerator dg = new DungeonGenerator(width, height);
		worldMap.setMap(new GameMap(dg.generate(TilesetType.CORNER_CAVES), true));
		
		worldMapEntity.add(worldMap);
		eng.addEntity(worldMapEntity);
		eng.getSystem(UniqueTagManager.class).set(Tags.WORLD_MAP, worldMapEntity);
		
		final HasMap screenMap = eng.createComponent(HasMap.class);
		
		final Entity screenMapEntity = eng.createEntity();
		screenMapEntity.add(screenMap);
		eng.addEntity(screenMapEntity);
		eng.getSystem(UniqueTagManager.class).set(Tags.SCREEN_MAP, screenMapEntity);
		
		final GreasedRegion floors = new GreasedRegion(worldMap.getMap().getSquidCharMap(), '.');
		for (int i = 0; i < 16; i++) {
			final Entity e = eng.createEntity();
			
			e.add(screenMap);
			
			final HasAppearance ha = eng.createComponent(HasAppearance.class);
			ha.setCh('@');
			ha.setColor(SColor.WHITE);
			e.add(ha);
			
			final CanMove canMove = eng.createComponent(CanMove.class);
			canMove.setSpeed(1f);
			e.add(canMove);
			
			final CanSee canSee = eng.createComponent(CanSee.class);
			canSee.setRadius(8);
			e.add(canSee);
			
			final CopiesFOVTo copyFOV = eng.createComponent(CopiesFOVTo.class);
			copyFOV.setCopyTo(eng.getSystem(EntityRefManager.class).get(screenMapEntity));
			e.add(copyFOV);
			
			final HasLocation hasLocation = eng.createComponent(HasLocation.class);
			hasLocation.setLocation(floors.singleRandom(RNG.get()));
			e.add(hasLocation);
			
			worldMap.getEntities().set(hasLocation.getLocation(), e);
			
			final HasBehavior hasBehavior = eng.createComponent(HasBehavior.class);
			hasBehavior.setBehavior(Behaviors.getDefault());
			e.add(hasBehavior);
			
			eng.addEntity(e);
		}
	}
}
