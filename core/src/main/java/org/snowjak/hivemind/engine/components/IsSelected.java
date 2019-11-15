/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} has been "selected" somehow.
 * 
 * @author snowjak88
 *
 */
public class IsSelected implements Component, Poolable {
	
	@Override
	public void reset() {
		
	}
}
