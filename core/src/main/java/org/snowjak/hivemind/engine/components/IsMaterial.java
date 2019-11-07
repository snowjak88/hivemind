/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.Materials;
import org.snowjak.hivemind.Materials.Material;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an {@link Entity} is composed of a {@link Material}.
 * 
 * @author snowjak88
 *
 */
public class IsMaterial implements Component, Poolable {
	
	private transient short material = -1;
	private String materialName = null;
	
	private int depth = 1;
	private float flowTimeRemaining = 0f;
	
	public Material getMaterial() {
		
		if (material < 0 && materialName != null)
			material = Materials.get().getIndex(materialName);
		
		return Materials.get().get(material);
	}
	
	public void setMaterial(Material material) {
		
		this.material = Materials.get().getIndex(material);
		materialName = (material == null) ? null : material.getName();
	}
	
	public int getDepth() {
		
		return depth;
	}
	
	public void setDepth(int depth) {
		
		this.depth = depth;
	}
	
	public float getFlowTimeRemaining() {
		
		return flowTimeRemaining;
	}
	
	public void setFlowTimeRemaining(float flowTimeRemaining) {
		
		this.flowTimeRemaining = flowTimeRemaining;
	}
	
	@Override
	public void reset() {
		
		material = -1;
		materialName = null;
		depth = 1;
		flowTimeRemaining = 0f;
	}
}
