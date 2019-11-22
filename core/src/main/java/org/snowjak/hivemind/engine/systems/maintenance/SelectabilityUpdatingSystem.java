/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.IsSelectable;
import org.snowjak.hivemind.engine.components.IsSelectableNow;
import org.snowjak.hivemind.engine.components.IsSelected;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.util.EntitySubscription;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidmath.Coord;

/**
 * Manages tagging {@link Entity Entities} which {@link IsSelectable} with
 * {@link IsSelectableNow}, when they, e.g. come into view of the POV Entity's
 * FOV.
 * 
 * @author snowjak88
 *
 */
public class SelectabilityUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<IsSelectableNow> SELECTABLE_NOW = ComponentMapper
			.getFor(IsSelectableNow.class);
	
	public SelectabilityUpdatingSystem() {
		
		super(Family.all(IsSelectable.class, HasLocation.class).get());
	}
	
	private final EntitySubscription selectable = new EntitySubscription(
			Family.one(IsSelectable.class, IsSelectableNow.class).get(), null, (e) -> {
				e.remove(IsSelectableNow.class);
				e.remove(IsSelected.class);
			});
	
	private HasFOV povFOV = null;
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		selectable.registerWith(engine);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		selectable.unregisterWith(engine);
		super.removedFromEngine(engine);
	}
	
	@Override
	public void update(float deltaTime) {
		
		final Entity povEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.POV);
		if (povEntity == null || !HAS_FOV.has(povEntity) || HAS_FOV.get(povEntity).getVisible() == null)
			return;
		
		povFOV = HAS_FOV.get(povEntity);
		
		super.update(deltaTime);
		
		povFOV = null;
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final Coord loc = HAS_LOCATION.get(entity).getLocation();
		
		if (loc == null || !povFOV.getVisible().contains(loc))
			entity.remove(IsSelectableNow.class);
		
		else if (!SELECTABLE_NOW.has(entity))
			entity.add(getEngine().createComponent(IsSelectableNow.class));
	}
}
