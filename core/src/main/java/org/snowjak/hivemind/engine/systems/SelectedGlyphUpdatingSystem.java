/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasSelectedGlyph;
import org.snowjak.hivemind.engine.components.HasUpdatedLocation;
import org.snowjak.hivemind.engine.components.IsSelected;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.GlyphAddedUpdate;
import org.snowjak.hivemind.gamescreen.updates.GlyphMovedUpdate;
import org.snowjak.hivemind.gamescreen.updates.GlyphRemovedUpdate;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;

import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.Coord;

/**
 * When an {@link Entity} has been selected, we must associate a Glyph with it,
 * allowing us to draw its "selection box".
 * 
 * @author snowjak88
 *
 */
public class SelectedGlyphUpdatingSystem extends IteratingSystem {
	
	public static final Color SELECTION_GLYPH_COLOR = SColor.CW_YELLOW;
	public static final char DEFAULT_SELECTION_CHAR = '\u25a1';
	
	private static final ComponentMapper<HasSelectedGlyph> SELECTED_GLYPH = ComponentMapper
			.getFor(HasSelectedGlyph.class);
	private static final ComponentMapper<HasAppearance> HAS_APPEARANCE = ComponentMapper.getFor(HasAppearance.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	private final EntityListener selectionChangeListener = new EntityListener() {
		
		@Override
		public void entityAdded(Entity entity) {
			
			final char selectionChar;
			if (HAS_APPEARANCE.has(entity))
				selectionChar = HAS_APPEARANCE.get(entity).getCh();
			else
				selectionChar = DEFAULT_SELECTION_CHAR;
			
			final Coord loc = HAS_LOCATION.get(entity).getLocation();
			
			final HasSelectedGlyph selectedGlyph = getEngine().createComponent(HasSelectedGlyph.class);
			selectedGlyph.setAwaitingCreation(true);
			entity.add(selectedGlyph);
			{
				final GlyphAddedUpdate upd = GameScreenUpdatePool.get().get(GlyphAddedUpdate.class);
				upd.setCh(selectionChar);
				upd.setColor(SELECTION_GLYPH_COLOR);
				upd.setX(loc.x);
				upd.setY(loc.y);
				upd.setConsumer(g -> {
					g.scaleBy(1.25f);
					selectedGlyph.setGlyph(g);
					selectedGlyph.setAwaitingCreation(false);
				});
				Context.getGameScreen().postGameScreenUpdate(upd);
			}
		}
		
		@Override
		public void entityRemoved(Entity entity) {
			
			if (!SELECTED_GLYPH.has(entity))
				return;
			
			final HasSelectedGlyph sg = SELECTED_GLYPH.get(entity);
			if (sg.getGlyph() == null)
				return;
			
			{
				final GlyphRemovedUpdate upd = GameScreenUpdatePool.get().get(GlyphRemovedUpdate.class);
				upd.setGlyph(sg.getGlyph());
				Context.getGameScreen().postGameScreenUpdate(upd);
			}
		}
		
	};
	
	public SelectedGlyphUpdatingSystem() {
		
		super(Family.all(HasSelectedGlyph.class, HasUpdatedLocation.class).get());
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		
		engine.addEntityListener(Family.all(IsSelected.class, HasLocation.class).get(), selectionChangeListener);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener(selectionChangeListener);
		
		super.removedFromEngine(engine);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final HasSelectedGlyph sg = SELECTED_GLYPH.get(entity);
		if (sg.isAwaitingCreation() || sg.getGlyph() == null)
			return;
		
		final Entity screenMapEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.SCREEN_MAP);
		if (screenMapEntity == null || !HAS_MAP.has(screenMapEntity))
			return;
		final HasMap screenMap = HAS_MAP.get(screenMapEntity);
		
		final Coord knownLocation = screenMap.getEntities().getLocation(entity);
		if (knownLocation == null)
			return;
		
		{
			final GlyphMovedUpdate upd = GameScreenUpdatePool.get().get(GlyphMovedUpdate.class);
			upd.setGlyph(sg.getGlyph());
			upd.setToX(knownLocation.x);
			upd.setToY(knownLocation.y);
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
	}
}
