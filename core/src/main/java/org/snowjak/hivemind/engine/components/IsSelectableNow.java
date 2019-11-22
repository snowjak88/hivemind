/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.util.loaders.IgnoreSerialization;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Denotes that an {@link Entity} can be selectable right now. Note that this is
 * distinct from {@link IsSelectable}, which merely marks an entity as the type
 * that could be selectable at some point.
 * 
 * @author snowjak88
 *
 */
@IgnoreSerialization
public class IsSelectableNow implements Component, Poolable {
	
	@Override
	public void reset() {
		
	}
}
