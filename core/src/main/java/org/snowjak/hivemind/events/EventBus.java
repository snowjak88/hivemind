/**
 * 
 */
package org.snowjak.hivemind.events;

import org.snowjak.hivemind.concurrent.Executor;

/**
 * Presents a singleton {@link com.google.common.eventbus.EventBus} instance.
 * 
 * @author snowjak88
 *
 */
public class EventBus extends com.google.common.eventbus.AsyncEventBus {
	
	private static EventBus __INSTANCE = null;
	
	/**
	 * Get the shared {@link EventBus} instance.
	 * 
	 * @return
	 */
	public static EventBus get() {
		
		if (__INSTANCE == null)
			synchronized (EventBus.class) {
				if (__INSTANCE == null)
					__INSTANCE = new EventBus();
			}
		return __INSTANCE;
	}
	
	private EventBus() {
		
		super(Executor.get());
	}
}
