/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that, when an {@link Entity} moves, it leaves behind instances of
 * the given prefab.
 * 
 * @author snowjak88
 *
 */
public class LeavesTrack implements Component, Poolable {
	
	private String prefabName = null;
	
	public String getPrefabName() {
		
		return prefabName;
	}
	
	public void setPrefabName(String prefabName) {
		
		this.prefabName = prefabName;
	}
	
	@Override
	public void reset() {
		
		prefabName = null;
	}
}
