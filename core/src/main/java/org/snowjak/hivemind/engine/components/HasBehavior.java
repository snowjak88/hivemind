/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} has some kind of behavior.
 * 
 * @author snowjak88
 *
 */
public class HasBehavior implements Component, Poolable {
	
	private BehaviorTree<Entity> behavior = null;
	
	public BehaviorTree<Entity> getBehavior() {
		
		return behavior;
	}
	
	public void setBehavior(BehaviorTree<Entity> behavior) {
		
		this.behavior = behavior;
	}
	
	@Override
	public void reset() {
		
		behavior = null;
	}
}
