/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * @author snowjak88
 *
 */
public interface TypeHierarchyLoader<T> extends JsonSerializer<T>, JsonDeserializer<T> {
	
}
