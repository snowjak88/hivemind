/**
 * 
 */
package org.snowjak.hivemind.gamescreen;

import squidpony.squidmath.Coord;

/**
 * Translates between screen- and map-grid-coordinates.
 * 
 * @author snowjak88
 *
 */
public interface ScreenMapTranslator {
	
	public default Coord screenToMap(Coord screen) {
		
		return Coord.get(screenToMapX(screen.x), screenToMapY(screen.y));
	}
	
	public int screenToMapX(int screenX);
	
	public int screenToMapY(int screenY);
	
	public default Coord mapToScreen(Coord map) {
		
		return Coord.get(mapToScreenX(map.x), mapToScreenY(map.y));
	}
	
	public int mapToScreenX(int mapX);
	
	public int mapToScreenY(int mapY);
}
