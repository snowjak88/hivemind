/**
 * 
 */
package org.snowjak.hivemind.engine;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Materials;
import org.snowjak.hivemind.Materials.Material;
import org.snowjak.hivemind.RNG;
import org.snowjak.hivemind.engine.components.CanMove;
import org.snowjak.hivemind.engine.components.CanSee;
import org.snowjak.hivemind.engine.components.CopiesFOVTo;
import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.HasBehavior;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.EntityRefManager;
import org.snowjak.hivemind.engine.systems.UniqueTagManager;
import org.snowjak.hivemind.map.GameMap;

import com.badlogic.ashley.core.Entity;

import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.PerlinNoise;

/**
 * Holds entity-world prefabs in code.
 * <p>
 * By convention, these static methods will merely <em>reconfigure</em> whatever
 * {@link Engine} you currently have held in your {@link Context}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class EnginePrefabs {
	
	public static void loadTest() {
		
		final int width = 64, height = 64;
		
		final Engine eng = Context.getEngine();
		eng.clear();
		
		final Entity worldMapEntity = eng.createEntity();
		
		final HasMap worldMap = eng.createComponent(HasMap.class);
		
		final Material[][] materials = new Material[width][height];
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				materials[x][y] = (PerlinNoise.noise((float) x / (float) width, (float) y / (float) height) + 1d)
						/ 2d > 0.5 ? Materials.get().get("stone") : Materials.get().get("earth");
			
		final DungeonGenerator dg = new DungeonGenerator(width, height);
		worldMap.setMap(new GameMap(dg.generate(TilesetType.CORNER_CAVES), materials, true));
		
		worldMapEntity.add(worldMap);
		eng.addEntity(worldMapEntity);
		eng.getSystem(UniqueTagManager.class).set(Tags.WORLD_MAP, worldMapEntity);
		
		final Entity screenMapEntity = eng.createEntity();
		screenMapEntity.add(eng.createComponent(HasMap.class));
		eng.addEntity(screenMapEntity);
		eng.getSystem(UniqueTagManager.class).set(Tags.SCREEN_MAP, screenMapEntity);
		
		final GreasedRegion floors = new GreasedRegion(worldMap.getMap().getSquidCharMap(), '.');
		for (int i = 0; i < 16; i++) {
			final Entity e = eng.createEntity();
			
			final HasAppearance ha = eng.createComponent(HasAppearance.class);
			ha.setCh('@');
			ha.setColor(SColor.AURORA_APRICOT);
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
			hasBehavior.setBehaviorName("default");
			e.add(hasBehavior);
			
			eng.addEntity(e);
		}
	}
}
