/**
 * 
 */
package org.snowjak.hivemind.concurrent;

import java.util.concurrent.Executors;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Provides access to a singleton {@link ListeningExecutorService} instance.
 * 
 * @author snowjak88
 *
 */
public class Executor {
	
	private static ListeningExecutorService __INSTANCE = null;
	
	/**
	 * @return the singleton {@link ListeningExecutorService}
	 */
	public static ListeningExecutorService get() {
		
		if (__INSTANCE == null)
			synchronized (Executor.class) {
				if (__INSTANCE == null) {
					__INSTANCE = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
					Runtime.getRuntime().addShutdownHook(new Thread(() -> __INSTANCE.shutdownNow()));
				}
			}
		
		return __INSTANCE;
	}
}
