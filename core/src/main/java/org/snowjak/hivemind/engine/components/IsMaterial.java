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
	
	public Material getMaterial() {
		
		if (material < 0 && materialName != null)
			material = Materials.get().getIndex(materialName);
		
		return Materials.get().get(material);
	}
	
	public void setMaterial(Material material) {
		
		this.material = Materials.get().getIndex(material);
		materialName = (material == null) ? null : material.getName();
	}
	
	@Override
	public void reset() {
		
		material = -1;
		materialName = null;
	}
}
