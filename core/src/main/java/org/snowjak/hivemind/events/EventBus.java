/**
 * 
 */
package org.snowjak.hivemind.events;

import java.util.function.Supplier;

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
	
	/**
	 * {@link #post(Object) Post} a new Event, first using {@link EventPool the
	 * EventPool} to obtain a new Event instance.
	 * <p>
	 * This offers a shortcut for when you don't need to configure your
	 * {@link Event} before posting it.
	 * </p>
	 * 
	 * @param eventType
	 */
	public <T extends Event> void post(Class<T> eventType) {
		
		this.post(EventPool.get().get(eventType));
	}
	
	/**
	 * {@link #post(Object) Post} a new Event, first using {@link EventPool the
	 * EventPool} to obtain a new Event instance.
	 * <p>
	 * This offers a shortcut for when you don't need to configure your
	 * {@link Event} before posting it.
	 * </p>
	 * 
	 * @param eventType
	 * @param constructor
	 */
	public <T extends Event> void post(Class<T> eventType, Supplier<T> constructor) {
		
		this.post(EventPool.get().get(eventType, constructor));
	}
	
}
