/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.util.ExtGreasedRegion;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} has had its FOV calculated.
 * 
 * @author snowjak88
 *
 */
public class HasFOV implements Component, Poolable {
	
	private ExtGreasedRegion visible = new ExtGreasedRegion(0, 0);
	
	public ExtGreasedRegion getVisible() {
		
		return visible;
	}
	
	public void setVisible(ExtGreasedRegion visible) {
		
		this.visible = visible;
	}
	
	@Override
	public void reset() {
		
		visible.clear();
	}
}
