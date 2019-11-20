/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} can sense "psychic-resonances". Also holds
 * that map of nearby psychic-energies.
 * 
 * @author snowjak88
 *
 */
public class CanSensePsychicEnergy implements Component, Poolable {
	
	private int range = 0;
	private double[][] map = new double[1][1];
	
	public int getRange() {
		
		return range;
	}
	
	public void setRange(int range) {
		
		this.range = range;
	}
	
	public double[][] getMap() {
		
		return map;
	}
	
	public void setMap(double[][] map) {
		
		this.map = map;
	}
	
	@Override
	public void reset() {
		
		range = 0;
		map = new double[1][1];
	}
}
