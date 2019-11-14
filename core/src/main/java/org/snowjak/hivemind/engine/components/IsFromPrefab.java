/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import org.snowjak.hivemind.engine.prefab.PrefabScript;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates the {@link PrefabScript} that was used to assemble an Entity, if
 * any.
 * 
 * @author snowjak88
 *
 */
public class IsFromPrefab implements Component, Poolable {
	
	private String name = null;
	
	public String getName() {
		
		return name;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
	
	@Override
	public void reset() {
		
		name = null;
	}
}
