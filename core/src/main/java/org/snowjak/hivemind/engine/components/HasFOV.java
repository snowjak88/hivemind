/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.util.ExtGreasedRegion;
import org.snowjak.hivemind.util.loaders.IgnoreSerialization;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} has had its FOV calculated.
 * 
 * @author snowjak88
 *
 */
@IgnoreSerialization
public class HasFOV implements Component, Poolable {
	
	private ExtGreasedRegion visible = new ExtGreasedRegion(1, 1), prevVisible = new ExtGreasedRegion(1, 1),
			visibleDelta = new ExtGreasedRegion(1, 1), noLongerVisible = new ExtGreasedRegion(1, 1);
	private double[][] lightLevels = new double[1][1];
	
	public ExtGreasedRegion getVisible() {
		
		return visible;
	}
	
	public void setVisible(ExtGreasedRegion visible) {
		
		this.visible = visible;
	}
	
	public ExtGreasedRegion getPrevVisible() {
		
		return prevVisible;
	}
	
	public void setPrevVisible(ExtGreasedRegion prevVisible) {
		
		this.prevVisible = prevVisible;
	}
	
	public ExtGreasedRegion getVisibleDelta() {
		
		return visibleDelta;
	}
	
	public void setVisibleDelta(ExtGreasedRegion visibleDelta) {
		
		this.visibleDelta = visibleDelta;
	}
	
	public ExtGreasedRegion getNoLongerVisible() {
		
		return noLongerVisible;
	}
	
	public void setNoLongerVisible(ExtGreasedRegion noLongerVisible) {
		
		this.noLongerVisible = noLongerVisible;
	}
	
	public double[][] getLightLevels() {
		
		return lightLevels;
	}
	
	public void setLightLevels(double[][] lightLevels) {
		
		this.lightLevels = lightLevels;
	}
	
	@Override
	public void reset() {
		
		visible.resizeAndEmpty(1, 1);
		prevVisible.resizeAndEmpty(1, 1);
		visibleDelta.resizeAndEmpty(1, 1);
		noLongerVisible.resizeAndEmpty(1, 1);
		lightLevels = new double[1][1];
	}
}
