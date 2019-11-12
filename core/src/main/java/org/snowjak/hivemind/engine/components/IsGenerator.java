/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Denotes that an {@link Entity} can generate other Entities, using a defined
 * prefab, at specified intervals.
 * <p>
 * By default, if the generator-Entity {@link HasLocation has a location}, all
 * Entities it generates are generated with that same location.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class IsGenerator implements Component, Poolable {
	
	private String prefab = null;
	private float interval = 0f;
	private float remainingInterval = 0f;
	
	public String getPrefab() {
		
		return prefab;
	}
	
	public void setPrefab(String prefab) {
		
		this.prefab = prefab;
	}
	
	public float getInterval() {
		
		return interval;
	}
	
	public void setInterval(float interval) {
		
		this.interval = interval;
	}
	
	public float getRemainingInterval() {
		
		return remainingInterval;
	}
	
	public void setRemainingInterval(float remainingInterval) {
		
		this.remainingInterval = remainingInterval;
	}
	
	@Override
	public void reset() {
		
		prefab = null;
		interval = 0f;
		remainingInterval = 0f;
	}
}
