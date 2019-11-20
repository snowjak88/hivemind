/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} has a "psychic resonance" -- i.e., a sort-of
 * smell given off by mind-activity.
 * 
 * @author snowjak88
 *
 */
public class HasPsychicResonance implements Component, Poolable {
	
	private int strength = 0;
	
	public int getStrength() {
		
		return strength;
	}
	
	public void setStrength(int strength) {
		
		this.strength = strength;
	}
	
	@Override
	public void reset() {
		
		strength = 0;
	}
}
