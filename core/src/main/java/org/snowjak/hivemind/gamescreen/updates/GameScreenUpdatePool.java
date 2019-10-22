/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.util.cache.TypedPool;

/**
 * Singleton pool for {@link GameScreenUpdate} instances.
 * 
 * @author snowjak88
 *
 */
public class GameScreenUpdatePool extends TypedPool<GameScreenUpdate> {
	
	private static GameScreenUpdatePool __INSTANCE = null;
	
	/**
	 * @return the singleton {@link GameScreenUpdatePool} instance
	 */
	public static GameScreenUpdatePool get() {
		
		if (__INSTANCE == null)
			synchronized (GameScreenUpdatePool.class) {
				if (__INSTANCE == null)
					__INSTANCE = new GameScreenUpdatePool();
			}
		return __INSTANCE;
	}
	
	private GameScreenUpdatePool() {
		
		super();
	}
}
