/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasAppearance;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasGlyph;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.gamescreen.updates.ClearMapUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.GlyphAddedUpdate;
import org.snowjak.hivemind.gamescreen.updates.GlyphColorChangeUpdate;
import org.snowjak.hivemind.gamescreen.updates.GlyphMovedUpdate;
import org.snowjak.hivemind.gamescreen.updates.GlyphRemovedUpdate;
import org.snowjak.hivemind.gamescreen.updates.MapDeltaUpdate;
import org.snowjak.hivemind.gamescreen.updates.MapScreenSizeUpdate;
import org.snowjak.hivemind.gamescreen.updates.MapUpdate;
import org.snowjak.hivemind.map.EntityMap;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

/**
 * This system will send {@link GameScreenUpdate}s to the {@link GameScreen}, if
 * there is an {@link Entity} tagged with {@link Tags#SCREEN_MAP} that
 * {@link HasMap has an associated GameMap}.
 * 
 * @author snowjak88
 *
 */
public class GameScreenUpdatingSystem extends EntitySystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(GameScreenUpdatingSystem.class.getName());
	
	/**
	 * Ordinarily, this system issues {@link MapDeltaUpdate}s to the
	 * {@link GameScreen}. However, periodically, this system will issue a (full)
	 * {@link MapUpdate}, just in case of ... I know not what.
	 */
	private static final float INTERVAL_FULL_UPDATE = 5f;
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	private static final ComponentMapper<HasAppearance> HAS_APPEARANCE = ComponentMapper.getFor(HasAppearance.class);
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	
	private static final ExtGreasedRegion emptyVisible = new ExtGreasedRegion(0, 0),
			visibleDelta = new ExtGreasedRegion(0, 0), prevVisible = new ExtGreasedRegion(0, 0);
	
	private final OrderedMap<Glyph, Entity> glyphToEntity = new OrderedMap<>();
	private boolean resetAllGlyphs = false;
	private float fullUpdateRemainingInterval = 0f;
	
	@Override
	public void update(float deltaTime) {
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		if (!utm.has(Tags.SCREEN_MAP))
			return;
		
		final Entity e = utm.get(Tags.SCREEN_MAP);
		
		updateMap(e, deltaTime);
		updateEntities(e, deltaTime);
	}
	
	private void updateMap(Entity screenMapEntity, float deltaTime) {
		
		final GameScreen gameScreen = Context.getGameScreen();
		if (gameScreen == null)
			return;
		//
		// If the tagged Entity has no associated map, then just clear the screen.
		if (!HAS_MAP.has(screenMapEntity)) {
			gameScreen.postGameScreenUpdate(GameScreenUpdatePool.get().get(ClearMapUpdate.class));
			
			return;
		}
		
		final HasMap hm = HAS_MAP.get(screenMapEntity);
		
		//
		// If the HasMap has no updated locations, then there's nothing to do.
		if (hm.getUpdatedLocations() == null || hm.getUpdatedLocations().isEmpty())
			return;
			
		//
		// If the HasMap has no good GameMap, then there's nothing to do.
		if (hm.getMap() == null)
			return;
			
		//
		// Now -- does the GameScreen need to be resized?
		if (gameScreen.getGridWidth() != hm.getMap().getWidth()
				|| gameScreen.getGridHeight() != hm.getMap().getHeight()) {
			final MapScreenSizeUpdate upd = GameScreenUpdatePool.get().get(MapScreenSizeUpdate.class);
			upd.setWidth(hm.getMap().getWidth());
			upd.setHeight(hm.getMap().getHeight());
			Context.getGameScreen().postGameScreenUpdate(upd);
			resetAllGlyphs = true;
		}
		
		//
		// Now -- compare the aggregate FOV (if available) against the previous FOV (if
		// available)
		final ExtGreasedRegion visible;
		if (HAS_FOV.has(screenMapEntity) && HAS_FOV.get(screenMapEntity).getVisible() != null)
			visible = HAS_FOV.get(screenMapEntity).getVisible();
		else
			visible = emptyVisible;
		
		if (prevVisible.width != visible.width || prevVisible.height != visible.height)
			prevVisible.resizeAndEmpty(visible.width, visible.height);
		
		if (visibleDelta.width != visible.width || visibleDelta.height != visible.height)
			visibleDelta.resizeAndEmpty(visible.width, visible.height);
		
		visibleDelta.remake(visible).xor(prevVisible);
		
		prevVisible.remake(visible);
		
		//
		// Add updates for all recently-updated locations.
		if ((fullUpdateRemainingInterval -= deltaTime) <= 0f) {
			final MapUpdate upd = GameScreenUpdatePool.get().get(MapUpdate.class);
			upd.setMap(hm.getMap(), visible);
			gameScreen.postGameScreenUpdate(upd);
			fullUpdateRemainingInterval = INTERVAL_FULL_UPDATE;
		} else {
			final MapDeltaUpdate upd = GameScreenUpdatePool.get().get(MapDeltaUpdate.class);
			upd.setMap(hm.getMap(), visible, visibleDelta.or(hm.getUpdatedLocations()));
			gameScreen.postGameScreenUpdate(upd);
		}
		
		//
		// Now that we've queued up all updates, reset that list of updated locations.
		hm.getUpdatedLocations().clear();
	}
	
	private void updateEntities(Entity screenMapEntity, float deltaTime) {
		
		final GameScreen gameScreen = Context.getGameScreen();
		if (gameScreen == null)
			return;
		
		if (!HAS_MAP.has(screenMapEntity))
			return;
		
		final HasMap hasMap = HAS_MAP.get(screenMapEntity);
		
		if (hasMap.getMap() == null)
			return;
		final GameMap map = hasMap.getMap();
		
		if (hasMap.getEntities() == null)
			hasMap.setEntities(new EntityMap());
		final EntityMap entities = hasMap.getEntities();
		
		final GreasedRegion fov;
		if (!HAS_FOV.has(screenMapEntity))
			fov = new GreasedRegion(map.getWidth(), map.getHeight());
		else
			fov = HAS_FOV.get(screenMapEntity).getVisible();
			
		//
		// 1) Create Glyphs for any Entities that have become visible.
		// 2) Remove Glyphs for any Entity that has been removed.
		// 3) Update the Glyph positions of all Entities that are currently visible.
		// 4) Ensure that any Entities outside the FOV have their Glyphs "ghosted".
		//
		final OrderedSet<Entity> updatedEntities;
		if (resetAllGlyphs) {
			updatedEntities = entities.getValues();
			resetAllGlyphs = false;
		} else
			updatedEntities = entities.getRecentlyUpdatedEntities();
		synchronized (glyphToEntity) {
			for (int i = 0; i < updatedEntities.size(); i++) {
				
				final Entity e = updatedEntities.getAt(i);
				
				//
				// Has this entity been added?
				if (!glyphToEntity.containsValue(e)) {
					if (!HAS_APPEARANCE.has(e))
						continue;
					final HasAppearance appearance = HAS_APPEARANCE.get(e);
					
					final HasGlyph hg;
					if (HAS_GLYPH.has(e)) {
						hg = HAS_GLYPH.get(e);
						
						if (hg.isAwaitingCreation())
							continue;
						
					} else {
						hg = getEngine().createComponent(HasGlyph.class);
						hg.setAwaitingCreation(true);
						e.add(hg);
					}
					
					if (hg.getGlyph() != null) {
						final GlyphRemovedUpdate upd = GameScreenUpdatePool.get().get(GlyphRemovedUpdate.class);
						upd.setGlyph(hg.getGlyph());
						gameScreen.postGameScreenUpdate(upd);
					}
					
					final Coord location = entities.getLocation(e);
					final GlyphAddedUpdate upd = GameScreenUpdatePool.get().get(GlyphAddedUpdate.class);
					upd.setCh(appearance.getCh());
					upd.setColor(appearance.getColor());
					upd.setX(location.x);
					upd.setY(location.y);
					upd.setConsumer((g) -> getEngine().getSystem(RunnableExecutingSystem.class).submit(() -> {
						synchronized (glyphToEntity) {
							hg.setGlyph(g);
							hg.setX(location.x);
							hg.setY(location.y);
							glyphToEntity.put(g, e);
							hg.setAwaitingCreation(false);
						}
					}));
					gameScreen.postGameScreenUpdate(upd);
					
				}
				//
				// Has this entity been removed?
				else if (glyphToEntity.containsValue(e) && entities.getLocation(e) == null) {
					
					if (!HAS_GLYPH.has(e))
						throw new IllegalStateException(
								"An entity is associated with a Glyph but has no HasGlyph component!");
					
					final HasGlyph hg = HAS_GLYPH.get(e);
					if (hg.getGlyph() == null)
						throw new IllegalStateException(
								"An entity is associated with a Glyph but its HasGlyph is not configured properly!");
					
					glyphToEntity.remove(hg.getGlyph());
					
					final GlyphRemovedUpdate upd = GameScreenUpdatePool.get().get(GlyphRemovedUpdate.class);
					upd.setGlyph(hg.getGlyph());
					gameScreen.postGameScreenUpdate(upd);
				} else {
					//
					// Has this entity's location been updated?
					if (glyphToEntity.containsValue(e) && entities.getLocation(e) != null) {
						
						if (!HAS_GLYPH.has(e))
							throw new IllegalStateException(
									"An entity is supposedly associated with a Glyph, but has no HasGlyph!");
						final HasGlyph hg = HAS_GLYPH.get(e);
						
						final Coord location = entities.getLocation(e);
						
						final GlyphMovedUpdate upd = GameScreenUpdatePool.get().get(GlyphMovedUpdate.class);
						upd.setFromX(hg.getX());
						upd.setFromY(hg.getY());
						upd.setToX(location.x);
						upd.setToY(location.y);
						upd.setGlyph(hg.getGlyph());
						upd.setMovementDuration(deltaTime);
						upd.setAfterMove(() -> getEngine().getSystem(RunnableExecutingSystem.class).submit(() -> {
							hg.setX(location.x);
							hg.setY(location.y);
						}));
						gameScreen.postGameScreenUpdate(upd);
					}
					
					//
					// Any Glyph within the FOV should be full-colored. Any Glyph outside the FOV
					// should be ghosted.
					if (entities.getLocation(e) != null) {
						if (!HAS_APPEARANCE.has(e))
							continue;
						
						final HasAppearance ha = HAS_APPEARANCE.get(e);
						
						if (!HAS_GLYPH.has(e))
							throw new IllegalStateException("An entity which is supposed to be known has no HasGlyph!");
						
						final HasGlyph hg = HAS_GLYPH.get(e);
						
						if (!HAS_GLYPH.has(e))
							throw new IllegalStateException(
									"An entity which is supposed to be known has no glyph within its HasGlyph!");
						
						final GlyphColorChangeUpdate upd = GameScreenUpdatePool.get().get(GlyphColorChangeUpdate.class);
						upd.setGlyph(hg.getGlyph());
						if (fov.contains(entities.getLocation(e)))
							upd.setNewColor(ha.getColor());
						else
							upd.setNewColor(ha.getGhostedColor());
						
						gameScreen.postGameScreenUpdate(upd);
					}
				}
			}
			
			entities.resetRecentlyUpdatedEntities();
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
