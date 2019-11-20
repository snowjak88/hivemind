/**
 * 
 */
package org.snowjak.hivemind;

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
	 * The {@link Entity} which represents the {@link GameScreen}'s Point-of-View.
	 */
	public static final String POV = "POV";
	
	/**
	 * The {@link Entity} which represents the player, and which should be the
	 * primary target of player-commands.
	 */
	public static final String PLAYER = "PLAYER";
	
	/**
	 * The {@link Entity} whose status is displayed on the sidebar.
	 */
	public static final String ACTIVE_INFO = "ACTIVE_INFO";
}
