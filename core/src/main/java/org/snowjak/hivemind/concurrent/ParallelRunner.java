/**
 * 
 */
package org.snowjak.hivemind.concurrent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Provides the ability to submit a number of {@link Runnable}s to the
 * {@link Executor common Executor} and wait for them to finish.
 * 
 * @author snowjak88
 *
 */
public class ParallelRunner {
	
	private static final Logger LOG = Logger.getLogger(ParallelRunner.class.getName());
	private final BlockingQueue<ListenableFuture<?>> futures = new LinkedBlockingQueue<>();
	
	/**
	 * Submit the given {@link Runnable} to the {@link Executor shared Executor} and
	 * add its associated {@link Future} to the internal list of Futures.
	 * 
	 * @param runnable
	 */
	public void add(Runnable runnable) {
		
		synchronized (futures) {
			futures.add(Executor.get().submit(runnable));
		}
	}
	
	/**
	 * Submit the given {@link Runnable}s to the {@link Executor shared Executor}
	 * and add their associated {@link Future}s to the internal list of Futures.
	 * 
	 * @param runnables
	 */
	public void add(Runnable... runnables) {
		
		for (Runnable r : runnables)
			add(r);
	}
	
	/**
	 * Submit the given {@link Runnable}s to the {@link Executor shared Executor}
	 * and add their associated {@link Future}s to the internal list of Futures.
	 * 
	 * @param runnables
	 */
	public void add(Iterable<Runnable> runnables) {
		
		for (Runnable r : runnables)
			add(r);
	}
	
	/**
	 * Wait for all currently-executing {@link Runnable}s to complete execution.
	 * <p>
	 * This will <strong>block</strong> the current thread until all {@link Future}s
	 * on the internal list are complete. Once all these Futures are complete, the
	 * internal list will be cleared.
	 * </p>
	 */
	public void awaitAll() {
		
		synchronized (this) {
			
			final Collection<ListenableFuture<?>> futures;
			synchronized (this.futures) {
				futures = new LinkedList<>();
				while (!this.futures.isEmpty()) {
					final ListenableFuture<?> f = this.futures.poll();
					if (f != null)
						futures.add(f);
				}
			}
			
			try {
				
				Futures.whenAllComplete(futures).run(() -> {
				}, MoreExecutors.directExecutor()).get();
				
			} catch (ExecutionException | InterruptedException e) {
				LOG.info("Interrupted while waiting for all submitted tasks to complete.");
			}
		}
	}
}
