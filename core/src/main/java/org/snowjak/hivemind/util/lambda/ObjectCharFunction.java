/**
 * 
 */
package org.snowjak.hivemind.util.lambda;

/**
 * A function accepting an object-reference and spitting out a {@code char}.
 * 
 * @author snowjak88
 *
 */
@FunctionalInterface
public interface ObjectCharFunction<U> {
	
	public char apply(U u);
}
