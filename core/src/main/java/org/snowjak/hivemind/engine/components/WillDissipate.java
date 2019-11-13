/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an Entity will disappear (i.e., be removed) after a certain
 * interval.
 * 
 * @author snowjak88
 *
 */
public class WillDissipate implements Component, Poolable {
	
	private float intervalRemaining = 0f;
	
	public float getIntervalRemaining() {
		
		return intervalRemaining;
	}
	
	public void setIntervalRemaining(float intervalRemaining) {
		
		this.intervalRemaining = intervalRemaining;
	}
	
	@Override
	public void reset() {
		
		intervalRemaining = 0f;
	}
}
