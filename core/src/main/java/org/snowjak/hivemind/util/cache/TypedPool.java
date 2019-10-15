/**
 * 
 */
package org.snowjak.hivemind.util.cache;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Supplier;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Implements a pool of instances for a variety of object-types, all sharing one
 * base-type.
 * 
 * @author snowjak88
 * @param T
 *            base-type for objects held by the pool
 *
 */
public abstract class TypedPool<T extends Poolable> {
	
	private final ObjectMap<Class<? extends T>, BlockingQueue<? extends T>> instances = new ObjectMap<>();
	
	/**
	 * Get a pooled instance from this pool, or create a new one if it doesn't exist
	 * using the default constructor.
	 * 
	 * @param <V>
	 * @param clazz
	 * @return
	 */
	public <V extends T> V get(Class<V> clazz) {
		
		return get(clazz, () -> {
			try {
				return clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Could not instantiate new instance for [" + clazz.getName() + "]!", e);
			}
		});
	}
	
	/**
	 * Get a pooled instance from this pool, or create a new one if it doesn't exist
	 * using the given constructor.
	 * 
	 * @param <V>
	 *            the specific type to get
	 * @param clazz
	 * @param constructor
	 *            the specific construction-method to use
	 * @return
	 */
	public <V extends T> V get(Class<V> clazz, Supplier<V> constructor) {
		
		final BlockingQueue<V> queue = getQueue(clazz);
		
		final V instance = queue.poll();
		if (instance != null)
			return instance;
		
		return constructor.get();
	}
	
	/**
	 * Retire the given instance from active service, dropping it back into the pool
	 * for subsequent use.
	 * 
	 * @param <T>
	 * @param instance
	 */
	@SuppressWarnings("unchecked")
	public <V extends T> void retire(V instance) {
		
		final BlockingQueue<V> queue = (BlockingQueue<V>) getQueue((Class<V>) instance.getClass());
		instance.reset();
		try {
			queue.put(instance);
		} catch (InterruptedException e) {
			throw new RuntimeException("Could not finish retiring an instance [" + instance.getClass().getName() + "]!",
					e);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <V extends T> BlockingQueue<V> getQueue(Class<V> clazz) {
		
		synchronized (instances) {
			if (!instances.containsKey(clazz))
				instances.put(clazz, new LinkedBlockingQueue<>());
			
			return (BlockingQueue<V>) instances.get(clazz);
		}
	}
}
