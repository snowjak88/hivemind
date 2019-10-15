/**
 * 
 */
package org.snowjak.hivemind.util.lambda;

/**
 * A function accepting 2 object-references and spitting out a {@code char}.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface BiObjectCharFunction<U, V> {
	
	public char apply(U u, V v);
}
