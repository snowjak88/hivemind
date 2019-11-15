/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.OrderedSet;
import squidpony.squidmath.SquidID;

/**
 * Indicates that an {@link Entity} will copy its {@link HasFOV FOV} to another
 * Entity.
 * 
 * @author snowjak88
 *
 */
public class CopiesFOVTo implements Component, Poolable {
	
	private OrderedSet<SquidID> copyTo = new OrderedSet<>();
	private int radius = 32767;
	
	public OrderedSet<SquidID> getCopyTo() {
		
		return copyTo;
	}
	
	public void addCopyTo(SquidID copyTo) {
		
		this.copyTo.add(copyTo);
	}
	
	public void removeCopyTo(SquidID copyTo) {
		
		this.copyTo.remove(copyTo);
	}
	
	public void setCopyTo(OrderedSet<SquidID> copyTo) {
		
		this.copyTo = copyTo;
	}
	
	public int getRadius() {
		
		return radius;
	}
	
	public void setRadius(int radius) {
		
		this.radius = radius;
	}
	
	@Override
	public void reset() {
		
		copyTo.clear();
		radius = 32767;
	}
}
