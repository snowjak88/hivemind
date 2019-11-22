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

/**
 * A very, very simple system that exists only to issue
 * {@link CenterScreenScrollAt} updates to the {@link GameScreen}, so as to
 * center the Entity tagged with {@link Tags#PLAYER}.
 * <p>
 * By default, this system will run only once before disabling itself.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PlayerCenteringSystem extends EntitySystem {
	
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		
		if (Context.getGameScreen() == null || Context.getGameScreen().getMapSurface() == null)
			return;
		
		final Entity playerEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.PLAYER);
		if (playerEntity == null || !HAS_LOCATION.has(playerEntity))
			return;
		
		final HasLocation hl = HAS_LOCATION.get(playerEntity);
		if (hl.getLocation() == null)
			return;
		
		System.out.println("Issuing center-screen-scroll-at update to " + hl.getLocation().toString());
		
		{
			final CenterScreenScrollAt upd = GameScreenUpdatePool.get().get(CenterScreenScrollAt.class);
			upd.setLocation(hl.getLocation());
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
		
		setProcessing(false);
	}
	
}
