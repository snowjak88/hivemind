/**
 * 
 */
package org.snowjak.hivemind.engine.systems.display;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.gamescreen.updates.CenterScreenScrollAt;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidmath.Coord;

/**
 * A very, very simple system that exists only to issue
 * {@link CenterScreenScrollAt} updates to the {@link GameScreen}, causing the
 * map-screen to be centered on the designated Entity.
 * <p>
 * By default, this system will run only once before disabling itself -- i.e.,
 * it will only issue one {@link CenterScreenScrollAt} update per call to one of
 * its activation methods:
 * <ul>
 * <li>{@link #centerOnPlayer()}</li>
 * <li>{@link #centerOn(String)}</li>
 * <li>{@link #centerOn(Coord)}</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class ScreenCenteringSystem extends EntitySystem {
	
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	private Coord location = null;
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		
		if (Context.getGameScreen() == null || Context.getGameScreen().getMapSurface() == null)
			return;
		
		System.out.println("Issuing center-screen-scroll-at update to " + location.toString());
		
		{
			final CenterScreenScrollAt upd = GameScreenUpdatePool.get().get(CenterScreenScrollAt.class);
			upd.setLocation(location);
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
		
		setProcessing(false);
	}
	
	/**
	 * Activates this {@link ScreenCenteringSystem} and configures it to center the
	 * screen on the {@link Entity} tagged with {@link Tags#PLAYER}.
	 * <p>
	 * If the {@link Tags#PLAYER PLAYER-tag} is not associated with any Entity, or
	 * if that Entity does not {@link HasLocation have a location}, this method does
	 * nothing.
	 * </p>
	 */
	public void centerOnPlayer() {
		
		centerOn(Tags.PLAYER);
	}
	
	/**
	 * Activates this {@link ScreenCenteringSystem} and configures it to center the
	 * screen on the {@link Entity} tagged with the given {@code tag}.
	 * <p>
	 * If the given tag is not associated with any Entity, or if that Entity does
	 * not {@link HasLocation have a location}, this method does nothing.
	 * </p>
	 * 
	 * @param tag
	 */
	public void centerOn(String tag) {
		
		final Entity taggedEntity = getEngine().getSystem(UniqueTagManager.class).get(tag);
		if (taggedEntity == null || !HAS_LOCATION.has(taggedEntity))
			return;
		
		centerOn(HAS_LOCATION.get(taggedEntity).getLocation());
	}
	
	/**
	 * Activates this {@link ScreenCenteringSystem} and configures it to center the
	 * screen on the given map-location.
	 * <p>
	 * If {@code location == null}, this method does nothing.
	 * </p>
	 * 
	 * @param location
	 */
	public void centerOn(Coord location) {
		
		if (location == null)
			return;
		
		this.location = location;
		setProcessing(true);
	}
}
