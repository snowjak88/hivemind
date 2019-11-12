/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.IsMaterial;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
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
	
	public AppearanceUpdatingSystem() {
		
		super(Family.all(HasAppearance.class).get(), INTERVAL);
	}
	
	@Override
	protected void processEntity(Entity entity) {
		
		final HasAppearance appearance = HAS_APPEARANCE.get(entity);
		final Color baseColor = appearance.getColor();
		
		appearance.setModifiedColor(baseColor);
		
		if (IS_MATERIAL.has(entity)) {
			final IsMaterial mat = IS_MATERIAL.get(entity);
			if (mat.getMaterial() != null && mat.getMaterial().getColor() != null)
				appearance.setModifiedColor(mat.getMaterial().getColor().mul(1f, 1f, 1f, (float) mat.getDepth()));
			
		}
	}
}
