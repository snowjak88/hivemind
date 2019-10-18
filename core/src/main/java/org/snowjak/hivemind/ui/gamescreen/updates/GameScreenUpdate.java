/**
 * 
 */
package org.snowjak.hivemind.ui.gamescreen.updates;

import org.snowjak.hivemind.ui.gamescreen.GameScreen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Base-type for a single update to this GameScreen.
 * 
 * @author snowjak88
 *
 */
public interface GameScreenUpdate extends Poolable {
	
	/**
	 * Update the GameScreen. This method will always be called on the
	 * rendering-thread, so there is no need for implementations to wrap their
	 * updates in calls to {@link Application#postRunnable(Runnable)
	 * Gdx.app.postRunnable(Runnable)}.
	 */
	public void execute(GameScreen gameScreen);
}