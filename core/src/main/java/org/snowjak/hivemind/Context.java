/**
 * 
 */
package org.snowjak.hivemind;

import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.gamescreen.GameScreen;

/**
 * Holder for global state. These instances cannot be simply created
 * once-and-for-all as singletons, because they may need to be discarded and
 * recreated.
 * 
 * @author snowjak88
 *
 */
public class Context {
	
	private static Context __INSTANCE = null;
	
	public static Context get() {
		
		if (__INSTANCE == null)
			synchronized (Context.class) {
				if (__INSTANCE == null)
					__INSTANCE = new Context();
			}
		return __INSTANCE;
	}
	
	private Engine engine = null;
	private GameScreen gameScreen = null;
	
	private Context() {
		
	}
	
	public static Engine getEngine() {
		
		return get().engine;
	}
	
	public static void setEngine(Engine engine) {
		
		get().engine = engine;
	}
	
	public static GameScreen getGameScreen() {
		
		return get().gameScreen;
	}
	
	public static void setGameScreen(GameScreen gameScreen) {
		
		get().gameScreen = gameScreen;
	}
}
