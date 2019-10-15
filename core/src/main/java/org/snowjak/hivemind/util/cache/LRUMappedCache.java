/**
 * 
 */
package org.snowjak.hivemind.util.cache;

import java.util.LinkedList;

import squidpony.squidmath.OrderedMap;

/**
 * Implements a Least-Recently-Used cache where cache-entries are retrieved via
 * some kind of identity-mapping.
 * 
 * @author snowjak88
 * @param <I>
 *            the identity-type, provided to retrieve mapped values
 * @param <E>
 *            the value-type
 */
public class LRUMappedCache<I, E> {
	
	private final LinkedList<I> usageOrder = new LinkedList<>();
	private final OrderedMap<I, E> values = new OrderedMap<>();
	private final int maxCacheEntries;
	
	private final CacheValuePersister<I, E> persister;
	private final CacheValueRestorer<I, E> restorer;
	private final CacheValueCreator<I, E> creator;
	
	/**
	 * Construct a new {@link LRUMappedCache}.
	 * 
	 * @param maxCacheEntries
	 *            the cache will be allowed to grow to no more than so-many entries
	 * @param persister
	 *            mechanism to persist entries which need to be swapped-out
	 * @param restorer
	 *            mechanism to restore entries which should be swapped-in (should
	 *            return {@code null} if restoration is impossible)
	 * @param creator
	 *            mechanism to create entries which cannot be restored
	 */
	public LRUMappedCache(int maxCacheEntries, CacheValuePersister<I, E> persister, CacheValueRestorer<I, E> restorer,
			CacheValueCreator<I, E> creator) {
		
		if (maxCacheEntries < 1)
			throw new IllegalArgumentException("Cannot create a new cache with fewer than 1 allowed entries!");
		
		this.maxCacheEntries = maxCacheEntries;
		this.persister = persister;
		this.restorer = restorer;
		this.creator = creator;
	}
	
	/**
	 * Get the cached value corresponding to the given identity. This method will
	 * {@link CacheValuePersister persist} as many entries as necessary to make room
	 * for the new value. The new value will be {@link CacheValueRestorer restored}
	 * if possible, {@link CacheValueCreator created} if restoration is not
	 * possible.
	 * 
	 * @param identity
	 * @return
	 */
	public E get(I identity) {
		
		synchronized (this) {
			if (!values.containsKey(identity)) {
				
				freeRoomInCache(1);
				
				final E restoredValue = restorer.restore(identity);
				values.put(identity, (restoredValue != null) ? restoredValue : creator.create(identity));
				
			} else
				usageOrder.remove(identity);
			
			usageOrder.addLast(identity);
			return values.get(identity);
		}
	}
	
	/**
	 * 
	 * @param identity
	 * @return {@code true} if this cache currently contains the given identity
	 */
	public boolean has(I identity) {
		
		synchronized (this) {
			return values.containsKey(identity);
		}
	}
	
	/**
	 * Stores the given value in the cache. This method will
	 * {@link CacheValuePersister persist} as many entries as necessary to make room
	 * for the new value.
	 * 
	 * @param identity
	 * @param value
	 */
	public void set(I identity, E value) {
		
		synchronized (this) {
			
			freeRoomInCache(1);
			
			usageOrder.remove(identity);
			usageOrder.addLast(identity);
			
			values.put(identity, value);
		}
	}
	
	private void freeRoomInCache(int slotsToFree) {
		
		while (values.size() > maxCacheEntries - slotsToFree && !values.isEmpty()) {
			final I identityToRemove = usageOrder.removeFirst();
			persister.persist(identityToRemove, values.get(identityToRemove));
			values.remove(identityToRemove);
		}
	}
	
	/**
	 * Provides the mechanism whereby {@link LRUMappedCache} entries are persisted
	 * before being replaced by other entries.
	 * 
	 * @author snowjak88
	 *
	 * @param <I>
	 * @param <E>
	 */
	@FunctionalInterface
	public interface CacheValuePersister<I, E> {
		
		public void persist(I identity, E value);
	}
	
	/**
	 * Provides the mechanism whereby {@link LRUMappedCache} entries are restored
	 * for storage in the cache.
	 * 
	 * @author snowjak88
	 *
	 * @param <I>
	 * @param <E>
	 */
	public interface CacheValueRestorer<I, E> {
		
		public E restore(I identity);
	}
	
	/**
	 * Provides the mechanism whereby {@link LRUMappedCache} entries can be created
	 * from whole cloth if necessary.
	 * 
	 * @author snowjak88
	 *
	 * @param <I>
	 * @param <E>
	 */
	public interface CacheValueCreator<I, E> {
		
		public E create(I identity);
	}
}
