/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} can move about the world.
 * 
 * @author snowjak88
 *
 */
public class CanMove implements Component, Poolable {
	
	private float speed = 1f;
	
	public float getSpeed() {
		
		return speed;
	}
	
	public void setSpeed(float speed) {
		
		this.speed = speed;
	}
	
	@Override
	public void reset() {
		
		speed = 1f;
	}
}
