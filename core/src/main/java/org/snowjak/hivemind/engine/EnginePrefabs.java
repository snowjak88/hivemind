/**
 * 
 */
package org.snowjak.hivemind.engine;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Materials;
import org.snowjak.hivemind.Materials.Material;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.components.CanSensePsychicEnergy;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.prefab.PrefabScript;
import org.snowjak.hivemind.engine.systems.RunnableExecutingSystem;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.gamescreen.updates.CenterScreenScrollAt;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.map.GameMap;

import com.badlogic.ashley.core.Entity;

import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.styled.TilesetType;
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
		
		final int width = 128, height = 128;
		
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
		
		final PrefabScript player = PrefabScript.byName("individual");
		player.run();
		player.faction("player");
		player.include("mixin/at-random-floor");
		player.tag(Tags.POV);
		player.tag(Tags.PLAYER);
		
		final CanSensePsychicEnergy sense = player.create(CanSensePsychicEnergy.class);
		sense.setRange(-1);
		
		for (int i = 0; i < 8; i++) {
			final PrefabScript ps = PrefabScript.byName("individual");
			ps.run();
			ps.include("mixin/at-random-floor");
			ps.include("mixin/screen-fov-sharing");
			ps.include("mixin/random-walker");
		}
	}
}
