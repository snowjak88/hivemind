/**
 * 
 */
package org.snowjak.hivemind.engine.systems.maintenance;

import org.snowjak.hivemind.Factions.Faction;
import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.IsMaterial;
import org.snowjak.hivemind.engine.systems.RunnableExecutingSystem;
import org.snowjak.hivemind.engine.systems.manager.FactionManager;

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
	public static final float INTERVAL = 3f;
	
	private static final ComponentMapper<HasAppearance> HAS_APPEARANCE = ComponentMapper.getFor(HasAppearance.class);
	private static final ComponentMapper<IsMaterial> IS_MATERIAL = ComponentMapper.getFor(IsMaterial.class);
	
	private final EntityListener appearanceAddedListener;
	
	public AppearanceUpdatingSystem() {
		
		super(Family.all(HasAppearance.class).get(), INTERVAL);
		
		appearanceAddedListener = new EntityListener() {
			
			@Override
			public void entityAdded(Entity entity) {
				
				getEngine().getSystem(RunnableExecutingSystem.class).submit(() -> updateEntityAppearance(entity));
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
	protected void updateInterval() {
		
		super.updateInterval();
	}
	
	@Override
	protected void processEntity(Entity entity) {
		
		updateEntityAppearance(entity);
	}
	
	public void updateEntityAppearance(Entity entity) {
		
		final FactionManager fm = getEngine().getSystem(FactionManager.class);
		
		final HasAppearance appearance = HAS_APPEARANCE.get(entity);
		final Color baseColor = appearance.getColor();
		
		appearance.setModifiedColor(baseColor);
		
		if (IS_MATERIAL.has(entity)) {
			final IsMaterial mat = IS_MATERIAL.get(entity);
			if (mat.getMaterial() != null && mat.getMaterial().getColor() != null)
				appearance.setModifiedColor(mat.getMaterial().getColor());
			
		}
		
		if (fm.has(entity)) {
			final Faction f = fm.get(entity);
			if (f.getColor() != null) {
				final Color tintedColor;
				if (appearance.getModifiedColor() != null)
					tintedColor = appearance.getModifiedColor().cpy().lerp(f.getColor(), 0.5f);
				else
					tintedColor = f.getColor();
				appearance.setModifiedColor(tintedColor);
			}
		}
	}
}
