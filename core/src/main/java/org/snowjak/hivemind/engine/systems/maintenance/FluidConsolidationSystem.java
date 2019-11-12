/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import java.util.ListIterator;

import org.snowjak.hivemind.Materials.Material;
import org.snowjak.hivemind.concurrent.BatchedRunner;
import org.snowjak.hivemind.concurrent.ParallelRunner;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasGlyph;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.IsMaterial;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.util.Profiler;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;

/**
 * For all {@link Entity Entities} at a given location that {@link IsMaterial
 * have the same material} that {@link Material#isFluid() is fluid} -- collapses
 * all such entities into a single Entity at that location.
 * 
 * @author snowjak88
 *
 */
public class FluidConsolidationSystem extends EntitySystem {
	
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<IsMaterial> IS_MATERIAL = ComponentMapper.getFor(IsMaterial.class);
	
	private final ParallelRunner parallel = new ParallelRunner();
	private final BatchedRunner batched = new BatchedRunner();
	
	public FluidConsolidationSystem() {
		
		super();
	}
	
	@Override
	public void update(float deltaTime) {
		
		final Profiler.ProfilerTimer timer = Profiler.get().start("FluidConsolidation (overall)");
		
		super.update(deltaTime);
		
		final Entity worldMapEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.WORLD_MAP);
		if (worldMapEntity == null)
			return;
		if (!HAS_MAP.has(worldMapEntity))
			return;
		final HasMap worldMap = HAS_MAP.get(worldMapEntity);
		
		for (int _x = 0; _x < worldMap.getMap().getWidth(); _x++)
			for (int _y = 0; _y < worldMap.getMap().getHeight(); _y++) {
				final int x = _x, y = _y;
				parallel.add(() -> {
					
					final OrderedMap<Material, Entity> fluidsAt = new OrderedMap<>();
					
					final ListIterator<Entity> iterator = worldMap.getEntities().getAt(Coord.get(x, y)).iterator();
					while (iterator.hasNext()) {
						
						final Entity e = iterator.next();
						
						if (!IS_MATERIAL.has(e) || !IS_MATERIAL.get(e).getMaterial().isFluid())
							continue;
						
						final IsMaterial mat = IS_MATERIAL.get(e);
						
						if (!fluidsAt.containsKey(mat.getMaterial())) {
							fluidsAt.put(mat.getMaterial(), e);
							continue;
						}
						
						final Entity contributeToEntity = fluidsAt.get(mat.getMaterial());
						final IsMaterial contributeTo = IS_MATERIAL.get(contributeToEntity);
						contributeTo.setDepth(contributeTo.getDepth() + mat.getDepth());
						
						worldMap.getEntities().markRefresh(contributeToEntity);
						
						batched.add(() -> getEngine().removeEntity(e));
					}
				});
			}
		
		parallel.awaitAll();
		batched.runUpdates();
		
		timer.stop();
	}
	
}
