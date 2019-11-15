/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.concurrent.BatchedRunner;
import org.snowjak.hivemind.engine.components.HasFOV;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.gamescreen.updates.ClearMapUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.MapDeltaUpdate;
import org.snowjak.hivemind.gamescreen.updates.MapScreenSizeUpdate;
import org.snowjak.hivemind.gamescreen.updates.MapUpdate;
import org.snowjak.hivemind.util.ExtGreasedRegion;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

/**
 * This system will send {@link GameScreenUpdate}s corresponding to all
 * (non-Entity) map-elements to the {@link GameScreen}, if there is an
 * {@link Entity} tagged with {@link Tags#SCREEN_MAP} that {@link HasMap has an
 * associated GameMap}.
 * 
 * @author snowjak88
 *
 */
public class MapUpdatingSystem extends EntitySystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MapUpdatingSystem.class.getName());
	
	/**
	 * Ordinarily, this system issues {@link MapDeltaUpdate}s to the
	 * {@link GameScreen}. However, periodically, this system will issue a (full)
	 * {@link MapUpdate}, just in case of ... I know not what.
	 */
	private static final float INTERVAL_FULL_UPDATE = 5f;
	
	private static final ExtGreasedRegion EMPTY_REGION = new ExtGreasedRegion();
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	
	private BatchedRunner batched = new BatchedRunner();
	
	private float fullUpdateRemainingInterval = 0f;
	
	/**
	 * A region that we pre-allocate, so we don't have to keep re-allocating it
	 * every frame (although we <em>do</em> check its size every frame).
	 */
	private ExtGreasedRegion scratch_region = new ExtGreasedRegion(1, 1);
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("MapUpdatingSystem (overall)");
		batched.runUpdates();
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		if (!utm.has(Tags.SCREEN_MAP))
			return;
		
		final Entity e = utm.get(Tags.SCREEN_MAP);
		
		updateMap(e, deltaTime);
		
		timer.stop();
	}
	
	private void updateMap(Entity screenMapEntity, float deltaTime) {
		
		final GameScreen gameScreen = Context.getGameScreen();
		if (gameScreen == null)
			return;
		//
		// If the tagged Entity has no associated map or FOV, then just clear the
		// screen.
		if (!HAS_MAP.has(screenMapEntity) || !HAS_FOV.has(screenMapEntity)) {
			gameScreen.postGameScreenUpdate(GameScreenUpdatePool.get().get(ClearMapUpdate.class));
			
			return;
		}
		
		final HasMap hm = HAS_MAP.get(screenMapEntity);
		final HasFOV fov = HAS_FOV.get(screenMapEntity);
		
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
		if (gameScreen.getMapGridWorldCellWidth() != hm.getMap().getWidth()
				|| gameScreen.getMapGridWorldCellHeight() != hm.getMap().getHeight()) {
			final MapScreenSizeUpdate upd = GameScreenUpdatePool.get().get(MapScreenSizeUpdate.class);
			upd.setWidth(hm.getMap().getWidth());
			upd.setHeight(hm.getMap().getHeight());
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
		
		//
		// Now -- compare the aggregate FOV (if available) against the previous FOV (if
		// available)
		final ExtGreasedRegion visible;
		if (HAS_FOV.has(screenMapEntity))
			visible = HAS_FOV.get(screenMapEntity).getVisible();
		else
			visible = EMPTY_REGION;
			
		//
		// Add updates for all recently-updated locations.
		if ((fullUpdateRemainingInterval -= deltaTime) <= 0f) {
			
			//
			// Time for a full-map update
			//
			
			final MapUpdate upd = GameScreenUpdatePool.get().get(MapUpdate.class);
			upd.setMap(hm.getMap(), visible);
			gameScreen.postGameScreenUpdate(upd);
			fullUpdateRemainingInterval = INTERVAL_FULL_UPDATE;
			
		} else {
			
			//
			// Perform only a map-delta update, including only those portions of the FOV
			// that have changed.
			//
			
			if (scratch_region.width != hm.getMap().getWidth() || scratch_region.height != hm.getMap().getHeight())
				scratch_region.resizeAndEmpty(hm.getMap().getWidth(), hm.getMap().getHeight());
			
			scratch_region.remake(fov.getVisibleDelta()).or(hm.getUpdatedLocations());
			
			final MapDeltaUpdate upd = GameScreenUpdatePool.get().get(MapDeltaUpdate.class);
			upd.setMap(hm.getMap(), visible, scratch_region);
			gameScreen.postGameScreenUpdate(upd);
		}
		
		//
		// Now that we've queued up all updates, reset that list of updated locations.
		hm.getUpdatedLocations().clear();
	}
}
