/**
 * 
 */
package org.snowjak.hivemind.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Provides the ability to assemble a number of {@link Runnable}s (potentially
 * across multiple threads) for subsequent execution by a single thread at some
 * fixed point (e.g., every frame).
 * 
 * @author snowjak88
 *
 */
public class BatchedRunner {
	
	private final BlockingQueue<Runnable> updates = new LinkedBlockingQueue<>();
	
	public void add(Runnable runnable) {
		
		try {
			updates.put(runnable);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Execute all pending updates. Blocks on the current thread until the internal
	 * queue is empty.
	 */
	public void runUpdates() {
		
		while (!updates.isEmpty()) {
			final Runnable runnable = updates.poll();
			if (runnable != null)
				runnable.run();
		}
	}
	
	/**
	 * Execute pending updates to a limit of {@code limit} before returning. Blocks
	 * on the current thread until the internal queue is empty.
	 * 
	 * @param limit
	 */
	public void runUpdates(int limit) {
		
		int i = 0;
		while (!updates.isEmpty() && i < limit) {
			final Runnable runnable = updates.poll();
			if (runnable != null) {
				runnable.run();
				i++;
			}
		}
	}
}