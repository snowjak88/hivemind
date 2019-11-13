/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.IsMaterial;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IntervalIteratingSystem;
import com.badlogic.gdx.graphics.Color;

/**
 * Maintains the visual properties for all {@link Entity Entities} that have an
 * {@link HasAppearance appearance} (which properties can be modified by all
 * sorts of other attributes).
 * <p>
 * Given {@link HasAppearance#getColor() HasAppearance.color}, sets
 * {@link HasAppearance#getModifiedColor() HasAppearance.modifiedColor}
 * </p>
 * 
 * @author snowjak88
 *
 */
public class AppearanceUpdatingSystem extends IntervalIteratingSystem {
	
	/**
	 * This system will execute every {@code INTERVAL} seconds.
	 */
	public static final float INTERVAL = 1f;
	
	private static final ComponentMapper<HasAppearance> HAS_APPEARANCE = ComponentMapper.getFor(HasAppearance.class);
	private static final ComponentMapper<IsMaterial> IS_MATERIAL = ComponentMapper.getFor(IsMaterial.class);
	
	private EntityListener appearanceAddedListener;
	
	public AppearanceUpdatingSystem() {
		
		super(Family.all(HasAppearance.class).get(), INTERVAL);
		
		appearanceAddedListener = new EntityListener() {
			
			@Override
			public void entityAdded(Entity entity) {
				
				updateEntityAppearance(entity);
			}
			
			@Override
			public void entityRemoved(Entity entity) {
				
				// nothing to do
			}
		};
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		
		engine.addEntityListener(Family.all(HasAppearance.class).get(), appearanceAddedListener);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener(appearanceAddedListener);
		
		super.removedFromEngine(engine);
	}
	
	@Override
	protected void processEntity(Entity entity) {
		
		updateEntityAppearance(entity);
	}
	
	public void updateEntityAppearance(Entity entity) {
		
		final HasAppearance appearance = HAS_APPEARANCE.get(entity);
		final Color baseColor = appearance.getColor();
		
		appearance.setModifiedColor(baseColor);
		
		if (IS_MATERIAL.has(entity)) {
			final IsMaterial mat = IS_MATERIAL.get(entity);
			if (mat.getMaterial() != null && mat.getMaterial().getColor() != null)
				appearance.setModifiedColor(mat.getMaterial().getColor());
			
		}
	}
}
