/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates whether the given {@link Entity} is one that can be selected or
 * not. (Some Entities may not be -- e.g., those that represent flying bullets,
 * or smoke, or other temporary visible things.)
 * 
 * @author snowjak88
 *
 */
public class IsSelectable implements Component, Poolable {
	
	@Override
	public void reset() {
		
	}
}
