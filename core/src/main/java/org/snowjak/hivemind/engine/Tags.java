/**
 * 
 */
package org.snowjak.hivemind.engine;

import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.gamescreen.GameScreen;

import com.badlogic.ashley.core.Entity;

/**
 * Holder for certain {@link Entity} tags we need to be able to refer to.
 * 
 * @author snowjak88
 * @see UniqueTagManager
 */
public class Tags {
	
	/**
	 * The {@link Entity} functioning as the world map will have this tag.
	 */
	public static final String WORLD_MAP = "WORLD_MAP";
	
	/**
	 * The {@link Entity} whose {@link HasMap} (if it has one) will be used to drive
	 * the {@link GameScreen}'s content.
	 */
	public static final String SCREEN_MAP = "SCREEN_MAP";
}
