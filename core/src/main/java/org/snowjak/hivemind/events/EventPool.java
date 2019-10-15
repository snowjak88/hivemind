/**
 * 
 */
package org.snowjak.hivemind.events;

import org.snowjak.hivemind.util.cache.TypedPool;

/**
 * A singleton {@link TypedPool pool} of {@link Event} instances.
 * 
 * @author snowjak88
 *
 */
public class EventPool extends TypedPool<Event> {
	
	private static EventPool __INSTANCE = null;
	
	/**
	 * @return the singleton {@link EventPool} instance
	 */
	public static EventPool get() {
		
		if (__INSTANCE == null)
			synchronized (EventPool.class) {
				if (__INSTANCE == null)
					__INSTANCE = new EventPool();
			}
		return __INSTANCE;
	}
}
