/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.concurrent.BatchedRunner;
import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasGlyph;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.IsSelected;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.GlyphAddedUpdate;
import org.snowjak.hivemind.gamescreen.updates.GlyphColorChangeUpdate;
import org.snowjak.hivemind.gamescreen.updates.GlyphMovedUpdate;
import org.snowjak.hivemind.gamescreen.updates.GlyphRemovedUpdate;
import org.snowjak.hivemind.map.EntityMap;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;
import org.snowjak.hivemind.util.SpatialMap.SpatialOperation;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.graphics.Color;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

/**
 * This system will send {@link GameScreenUpdate}s corresponding to those
 * Entities represented by {@link Glyph}s to the {@link GameScreen}, if there is
 * an {@link Entity} tagged with {@link Tags#POV} that {@link HasMap has
 * an associated GameMap}.
 * 
 * @author snowjak88
 *
 */
public class GlyphUpdatingSystem extends EntitySystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(GlyphUpdatingSystem.class.getName());
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<HasAppearance> HAS_APPEARANCE = ComponentMapper.getFor(HasAppearance.class);
	private static final ComponentMapper<IsSelected> IS_SELECTED = ComponentMapper.getFor(IsSelected.class);
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	
	private BatchedRunner batched = new BatchedRunner();
	
	private final OrderedMap<Entity, Glyph> entityToGlyph = new OrderedMap<>();
	private final OrderedMap<Glyph, Entity> glyphToEntity = new OrderedMap<>();
	
	/**
	 * When an Entity which {@link HasGlyph has a Glyph} is removed from the Engine,
	 * we need to ensure that we issue a Glyph-Removal update to the GameScreen.
	 */
	private final EntityListener glyphRemovingEntityListener;
	
	public GlyphUpdatingSystem() {
		
		super();
		glyphRemovingEntityListener = new EntityListener() {
			
			@Override
			public void entityAdded(Entity entity) {
				
				// Nothing to do
			}
			
			@Override
			public void entityRemoved(Entity entity) {
				
				if (Context.getGameScreen() == null)
					return;
				
				if (entityToGlyph.containsKey(entity)) {
					
					final GlyphRemovedUpdate upd = GameScreenUpdatePool.get().get(GlyphRemovedUpdate.class);
					upd.setGlyph(entityToGlyph.get(entity));
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
			}
		};
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		engine.addEntityListener(Family.all(HasGlyph.class).get(), glyphRemovingEntityListener);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener(glyphRemovingEntityListener);
		super.removedFromEngine(engine);
	}
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("MapUpdatingSystem (overall)");
		batched.runUpdates();
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		if (!utm.has(Tags.POV))
			return;
		
		final Entity e = utm.get(Tags.POV);
		
		updateEntities(e, deltaTime);
		
		timer.stop();
	}
	
	private void updateEntities(Entity povEntity, float deltaTime) {
		
		final GameScreen gameScreen = Context.getGameScreen();
		if (gameScreen == null)
			return;
		
		if (!HAS_MAP.has(povEntity) || !HAS_FOV.has(povEntity))
			return;
		
		final HasMap hasMap = HAS_MAP.get(povEntity);
		if (hasMap.getMap() == null)
			return;
		final EntityMap entities = hasMap.getEntities();
		
		final HasFOV fov = HAS_FOV.get(povEntity);
		
		//
		// Process the recent updates in the screen-map's EntityMap.
		//
		// * ADDED --> add a glyph for the entity
		// * MOVED --> move the glyph (if visible)
		// * REFRESH --> refresh the glyph's color and location, if present
		// * REMOVED --> remove the glyph
		//
		// Complicating this is that the glyph's appearance will be altered
		// if it is invisible or its known location is not accurate: it will be
		// "ghosted", made translucent and somewhat gray.
		//
		{
			final OrderedSet<Entity> added = entities.getRecentlyUpdated(SpatialOperation.ADDED);
			for (int i = 0; i < added.size(); i++) {
				
				final Entity e = added.getAt(i);
				
				if (!HAS_APPEARANCE.has(e) || !HAS_LOCATION.has(e))
					continue;
				
				if (HAS_GLYPH.has(e))
					continue;
				
				final Coord trueLocation = HAS_LOCATION.get(e).getLocation();
				final Coord knownLocation = entities.getLocation(e);
				final boolean isLocationAccurate = trueLocation.equals(knownLocation);
				final boolean isVisible = fov.getVisible().contains(knownLocation);
				final HasAppearance ha = HAS_APPEARANCE.get(e);
				
				final HasGlyph hg = getEngine().createComponent(HasGlyph.class);
				hg.setAwaitingCreation(true);
				hg.setX(knownLocation.x);
				hg.setY(knownLocation.y);
				e.add(hg);
				
				final Color color = (isVisible && isLocationAccurate) ? ha.getModifiedColor() : ha.getGhostedColor();
				if (color == null)
					continue;
				
				{
					final GlyphAddedUpdate upd = GameScreenUpdatePool.get().get(GlyphAddedUpdate.class);
					upd.setCh(ha.getCh());
					upd.setColor(color);
					upd.setX(knownLocation.x);
					upd.setY(knownLocation.y);
					upd.setConsumer((g) -> batched.add(() -> {
						hg.setGlyph(g);
						hg.setAwaitingCreation(false);
						entityToGlyph.put(e, g);
						glyphToEntity.put(g, e);
					}));
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
				
				entities.resetRecentlyUpdated(SpatialOperation.ADDED, e);
			}
		}
		
		{
			final OrderedSet<Entity> moved = entities.getRecentlyUpdated(SpatialOperation.MOVED);
			for (int i = 0; i < moved.size(); i++) {
				
				final Entity e = moved.getAt(i);
				
				if (!HAS_APPEARANCE.has(e) || !HAS_LOCATION.has(e))
					continue;
				
				if (!HAS_GLYPH.has(e))
					continue;
				
				final HasGlyph hg = HAS_GLYPH.get(e);
				if (hg.isAwaitingCreation())
					continue;
				
				final Coord trueLocation = HAS_LOCATION.get(e).getLocation();
				final Coord knownLocation = entities.getLocation(e);
				final boolean isLocationAccurate = trueLocation.equals(knownLocation);
				final boolean isVisible = fov.getVisible().contains(knownLocation);
				final HasAppearance ha = HAS_APPEARANCE.get(e);
				
				final Color color = (isVisible && isLocationAccurate) ? ha.getModifiedColor() : ha.getGhostedColor();
				if (color == null)
					continue;
				
				{
					final GlyphMovedUpdate upd = GameScreenUpdatePool.get().get(GlyphMovedUpdate.class);
					upd.setToX(knownLocation.x);
					upd.setToY(knownLocation.y);
					upd.setGlyph(hg.getGlyph());
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
				
				{
					final GlyphColorChangeUpdate upd = GameScreenUpdatePool.get().get(GlyphColorChangeUpdate.class);
					upd.setGlyph(hg.getGlyph());
					upd.setNewColor(color);
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
				
				entities.resetRecentlyUpdated(SpatialOperation.MOVED, e);
			}
		}
		
		{
			final OrderedSet<Entity> refreshed = entities.getRecentlyUpdated(SpatialOperation.REFRESH);
			for (int i = 0; i < refreshed.size(); i++) {
				
				final Entity e = refreshed.getAt(i);
				
				if (!HAS_APPEARANCE.has(e) || !HAS_LOCATION.has(e))
					continue;
				
				if (!HAS_GLYPH.has(e))
					continue;
				
				final HasGlyph hg = HAS_GLYPH.get(e);
				
				if (hg.isAwaitingCreation())
					continue;
				
				final Coord trueLocation = HAS_LOCATION.get(e).getLocation();
				final Coord knownLocation = entities.getLocation(e);
				final boolean isLocationAccurate = trueLocation.equals(knownLocation);
				final boolean isVisible = fov.getVisible().contains(knownLocation);
				final HasAppearance ha = HAS_APPEARANCE.get(e);
				
				final Color color = (isVisible && isLocationAccurate) ? ha.getModifiedColor() : ha.getGhostedColor();
				if (color == null)
					continue;
				
				{
					final GlyphMovedUpdate upd = GameScreenUpdatePool.get().get(GlyphMovedUpdate.class);
					upd.setToX(knownLocation.x);
					upd.setToY(knownLocation.y);
					upd.setGlyph(hg.getGlyph());
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
				
				{
					final GlyphColorChangeUpdate upd = GameScreenUpdatePool.get().get(GlyphColorChangeUpdate.class);
					upd.setGlyph(hg.getGlyph());
					upd.setNewColor(color);
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
				
				entities.resetRecentlyUpdated(SpatialOperation.REFRESH, e);
			}
		}
		
		{
			final OrderedSet<Entity> removed = entities.getRecentlyUpdated(SpatialOperation.REMOVED);
			for (int i = 0; i < removed.size(); i++) {
				
				final Entity e = removed.getAt(i);
				
				if (!HAS_GLYPH.has(e))
					continue;
				
				final HasGlyph hg = HAS_GLYPH.get(e);
				if (hg.isAwaitingCreation())
					continue;
				
				{
					final GlyphRemovedUpdate upd = GameScreenUpdatePool.get().get(GlyphRemovedUpdate.class);
					upd.setGlyph(hg.getGlyph());
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
				
				final Glyph oldGlyph = entityToGlyph.remove(e);
				glyphToEntity.remove(oldGlyph);
				
				e.remove(HasGlyph.class);
				
				entities.resetRecentlyUpdated(SpatialOperation.REMOVED, e);
			}
		}
		
		{
			//
			// Finally -- check on entities which are in "previously-visible" and not in
			// "visible". These entities should be drawn as "ghosted".
			//
			final OrderedSet<Entity> noLongerVisibleEntities = entities.getWithin(fov.getNoLongerVisible());
			
			for (int i = 0; i < noLongerVisibleEntities.size(); i++) {
				final Entity e = noLongerVisibleEntities.getAt(i);
				
				if (!HAS_APPEARANCE.has(e) || !HAS_LOCATION.has(e))
					continue;
				
				if (!HAS_GLYPH.has(e))
					continue;
				
				final HasGlyph hg = HAS_GLYPH.get(e);
				if (hg.isAwaitingCreation())
					continue;
				
				final HasAppearance ha = HAS_APPEARANCE.get(e);
				
				if (ha.getGhostedColor() == null)
					continue;
				
				{
					final GlyphColorChangeUpdate upd = GameScreenUpdatePool.get().get(GlyphColorChangeUpdate.class);
					upd.setGlyph(hg.getGlyph());
					upd.setNewColor(ha.getGhostedColor());
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
			}
		}
	}
	
	/**
	 * Get the {@link Entity} associated with the given {@link Glyph}, or
	 * {@code null} if no such association exists.
	 * 
	 * @param glyph
	 * @return
	 */
	public Entity getEntityFor(Glyph glyph) {
		
		synchronized (glyphToEntity) {
			return glyphToEntity.get(glyph);
		}
	}
}
