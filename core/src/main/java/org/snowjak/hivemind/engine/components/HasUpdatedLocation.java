/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} has changed its {@link HasLocation
 * location}.
 * 
 * @author snowjak88
 *
 */
public class HasUpdatedLocation implements Component, Poolable {
	
	private int tickCounter = 0;
	
	public void incrementTickCounter() {
		
		tickCounter++;
	}
	
	public int getTickCounter() {
		
		return tickCounter;
	}
	
	@Override
	public void reset() {
		
		this.tickCounter = 0;
	}
}
