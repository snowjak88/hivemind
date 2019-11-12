/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Materials.Material;
import org.snowjak.hivemind.RNG;
import org.snowjak.hivemind.concurrent.BatchedRunner;
import org.snowjak.hivemind.concurrent.ParallelRunner;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.IsMaterial;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.util.EntityUtil;
import org.snowjak.hivemind.util.Profiler;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * For all {@link Entity Entities} that {@link IsMaterial are made from} a
 * {@link Material} which {@link Material#isFluid() is a fluid}, implements
 * fluid-flow:
 * <p>
 * Picks a valid neighboring cell and allows 1 unit of the Material to "flow
 * into" that cell.
 * </p>
 * <p>
 * A cell is valid if:
 * <ul>
 * <li>Its terrain-type is not a wall</li>
 * <li>If it contains an Entity made of the same material -- that Entity's
 * fluid-level is less than this Entity's fluid-level</li>
 * </ul>
 * If that "flowing into" deprives this Entity of all fluid-level, then this
 * Entity is removed.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class FluidFlowSystem extends EntitySystem {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<IsMaterial> IS_MATERIAL = ComponentMapper.getFor(IsMaterial.class);
	
	private final ParallelRunner parallel = new ParallelRunner();
	private final BatchedRunner batched = new BatchedRunner();
	
	@Override
	public void update(float deltaTime) {
		
		final Profiler.ProfilerTimer timer = Profiler.get().start("FluidFlow (overall)");
		
		final Entity worldMapEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.WORLD_MAP);
		if (worldMapEntity == null)
			return;
		if (!HAS_MAP.has(worldMapEntity))
			return;
		final HasMap worldMap = HAS_MAP.get(worldMapEntity);
		if (worldMap.getMap() == null || worldMap.getEntities() == null)
			return;
		
		for (int _x = 0; _x < worldMap.getMap().getWidth(); _x++)
			for (int _y = 0; _y < worldMap.getMap().getHeight(); _y++) {
				
				final int x = _x, y = _y;
				parallel.add(() -> {
					final OrderedSet<Entity> examineEntities = worldMap.getEntities().getAt(Coord.get(x, y));
					
					for (int i = 0; i < examineEntities.size(); i++) {
						final Entity flowFromEntity = examineEntities.getAt(i);
						if (!IS_MATERIAL.has(flowFromEntity))
							continue;
						final IsMaterial material = IS_MATERIAL.get(flowFromEntity);
						if (material.getMaterial() == null)
							continue;
						if (!material.getMaterial().isFluid())
							continue;
						if (material.getDepth() <= 1)
							continue;
							
						//
						// Scan neighboring cells to look for valid cells to flow into.
						//
						final OrderedSet<Coord> validCells = new OrderedSet<>(8);
						
						final int startNX = (x > 0) ? x - 1 : 0;
						final int startNY = (y > 0) ? y - 1 : 0;
						final int endNX = (x + 1 < worldMap.getMap().getWidth()) ? x + 1
								: worldMap.getMap().getWidth() - 1;
						final int endNY = (y + 1 < worldMap.getMap().getHeight()) ? y + 1
								: worldMap.getMap().getHeight() - 1;
						for (int nx = startNX; nx <= endNX; nx++)
							for (int ny = startNY; ny <= endNY; ny++) {
								if (nx == x && ny == y)
									continue;
								if (worldMap.getMap().getTerrain(nx, ny).getSquidChar() == '#')
									continue;
								
								boolean skip = false;
								final OrderedSet<Entity> neighbors = worldMap.getEntities().getAt(Coord.get(x, y));
								for (int j = 0; j < neighbors.size(); j++) {
									final Entity neighbor = neighbors.getAt(j);
									if (IS_MATERIAL.has(neighbor))
										if (IS_MATERIAL.get(neighbor).getMaterial() == material.getMaterial())
											if (IS_MATERIAL.get(neighbor).getDepth() >= material.getDepth()) {
												skip = true;
												break;
											}
								}
								
								if (!skip)
									validCells.add(Coord.get(nx, ny));
							}
						
						if (validCells.isEmpty())
							continue;
						
						material.setFlowTimeRemaining(material.getFlowTimeRemaining() - deltaTime);
						if (material.getFlowTimeRemaining() > 0f)
							continue;
						
						final Coord flowInto = validCells.randomItem(RNG.get());
						
						batched.add(() -> {
							//
							// Determine if the "flow-into" cell already has a material of the required
							// type.
							IsMaterial flowIntoMaterial = null;
							
							final OrderedSet<Entity> entitiesAt = worldMap.getEntities().getAt(flowInto);
							for (int u = 0; u < entitiesAt.size(); u++) {
								final Entity entityAt = entitiesAt.getAt(u);
								if (!IS_MATERIAL.has(entityAt))
									continue;
								final IsMaterial materialAt = IS_MATERIAL.get(entityAt);
								if (materialAt.getMaterial() != material.getMaterial())
									continue;
								flowIntoMaterial = materialAt;
								break;
							}
							
							if (flowIntoMaterial == null) {
								final Entity flowIntoEntity = EntityUtil.clone(flowFromEntity);
								
								flowIntoMaterial = getEngine().createComponent(IsMaterial.class);
								flowIntoEntity.add(flowIntoMaterial);
								flowIntoMaterial.setMaterial(material.getMaterial());
								
								Context.getEngine().addEntity(flowIntoEntity);
							}
							
							final float flowTime = (material.getMaterial().getFlowSpeed() <= 0) ? Float.MAX_VALUE
									: 1f / material.getMaterial().getFlowSpeed();
							material.setFlowTimeRemaining(flowTime);
							flowIntoMaterial.setFlowTimeRemaining(flowTime);
							
							flowIntoMaterial.setDepth(flowIntoMaterial.getDepth() + 1);
							material.setDepth(material.getDepth() - 1);
						});
					}
				});
			}
		
		parallel.awaitAll();
		batched.runUpdates();
		
		timer.stop();
	}
	
}
