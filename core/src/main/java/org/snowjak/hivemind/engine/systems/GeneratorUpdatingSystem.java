/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.logging.Logger;

import org.codehaus.groovy.runtime.StringBufferWriter;
import org.snowjak.hivemind.concurrent.BatchedRunner;
import org.snowjak.hivemind.concurrent.ParallelRunner;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.IsGenerator;
import org.snowjak.hivemind.engine.prefab.PrefabScript;
import org.snowjak.hivemind.engine.systems.manager.EntityRefManager;
import org.snowjak.hivemind.json.Json;
import org.snowjak.hivemind.util.Profiler;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidmath.SquidID;

/**
 * Updates all {@link Entity Entities} that {@link IsGenerator are generators}
 * for other Entities.
 * 
 * @author snowjak88
 *
 */
public class GeneratorUpdatingSystem extends IteratingSystem {
	
	private static final Logger LOG = Logger.getLogger(GeneratorUpdatingSystem.class.getName());
	
	private static final ComponentMapper<IsGenerator> IS_GENERATOR = ComponentMapper.getFor(IsGenerator.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	private final ParallelRunner parallel = new ParallelRunner();
	private final BatchedRunner batched = new BatchedRunner();
	
	public GeneratorUpdatingSystem() {
		
		super(Family.all(IsGenerator.class).get());
	}
	
	@Override
	public void update(float deltaTime) {
		
		final Profiler.ProfilerTimer timer = Profiler.get().start("GeneratorUpdating (overall)");
		
		super.update(deltaTime);
		
		parallel.awaitAll();
		batched.runUpdates();
		
		timer.stop();
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		parallel.add(() -> {
			final IsGenerator generator = IS_GENERATOR.get(entity);
			
			if (generator.getPrefab() == null) {
				final String entityID = getEntityID(entity);
				LOG.warning("Generator-entity " + ((entityID.isEmpty()) ? entityID : "(" + entityID + ") ")
						+ "has no configured prefab-name, and so will never generate anything!");
				return;
			}
			
			generator.setRemainingInterval(generator.getRemainingInterval() - deltaTime);
			if (generator.getRemainingInterval() <= 0f) {
				final PrefabScript prefab = PrefabScript.byName(generator.getPrefab());
				
				if (prefab == null) {
					final String entityID = getEntityID(entity);
					LOG.warning("Generator-entity " + ((entityID.isEmpty()) ? entityID : "(" + entityID + ") ")
							+ "has a configured prefab-name (\"" + generator.getPrefab()
							+ "\") that does not map to any known prefab!");
					return;
				}
				
				batched.add(() -> {
					final Entity generated = prefab.run();
					
					if (HAS_LOCATION.has(entity)) {
						final HasLocation loc = getEngine().createComponent(HasLocation.class);
						loc.setLocation(HAS_LOCATION.get(entity).getLocation());
						loc.setFractionalX(HAS_LOCATION.get(entity).getFractionalX());
						loc.setFractionalY(HAS_LOCATION.get(entity).getFractionalY());
						generated.add(loc);
					}
					
				});
				generator.setRemainingInterval(generator.getInterval());
			}
		});
	}
	
	private String getEntityID(Entity entity) {
		
		final EntityRefManager erm = getEngine().getSystem(EntityRefManager.class);
		if (erm.has(entity)) {
			
			final SquidID id = erm.get(entity);
			
			final StringBuffer buf = new StringBuffer();
			Json.get().toJson(id, new StringBufferWriter(buf));
			return buf.toString();
			
		} else
			return "";
	}
}
