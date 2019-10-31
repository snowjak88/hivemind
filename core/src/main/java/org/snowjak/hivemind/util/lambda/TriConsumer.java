/**
 * 
 */
package org.snowjak.hivemind.util.lambda;

import java.util.function.BiConsumer;

/**
 * Extension of {@link BiConsumer} to three arguments.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface TriConsumer<T, U, V> {
	
	/**
	 * Performs this operation on the given arguments.
	 */
	public void accept(T t, U u, V v);
}
