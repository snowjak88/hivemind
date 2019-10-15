/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target({ TYPE, FIELD })
/**
 * Denotes that a particular type should be ignored entirely when serializing or
 * deserializing.
 * 
 * @author snowjak88
 *
 */
public @interface IgnoreSerialization {
	
}
