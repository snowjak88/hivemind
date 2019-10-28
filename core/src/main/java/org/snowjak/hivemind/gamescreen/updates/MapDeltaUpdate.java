/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.map.GameMap;

import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;

/**
 * A {@link MapUpdate} which only updates <em>part</em> of the screen.
 * 
 * @author snowjak88
 *
 */
public class MapDeltaUpdate extends MapUpdate {
	
	protected GreasedRegion update = new GreasedRegion(0, 0);
	private Coord[] updateCoords = null;
	
	public void setMap(GameMap map, GreasedRegion visible, GreasedRegion update) {
		
		resize(map.getWidth(), map.getHeight());
		
		updateCoords = update.asCoords();
		
		synchronized (map) {
			for (int i = 0; i < updateCoords.length; i++)
				terrain[updateCoords[i].x][updateCoords[i].y] = map.getTerrainIndex(updateCoords[i].x,
						updateCoords[i].y);
		}
		
		this.visible.remake(visible).and(update);
		this.update.remake(update);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated use {@link #setMap(GameMap, GreasedRegion)} to specify the
	 *             update-region
	 */
	@Deprecated
	@Override
	public void setMap(GameMap map, GreasedRegion visible) {
		
		super.setMap(map, visible);
	}
	
	@Override
	protected void resize(int width, int height) {
		
		super.resize(width, height);
		
		update.resizeAndEmpty(width, height);
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		for (int i = 0; i < updateCoords.length; i++)
			drawCell(gameScreen, updateCoords[i].x, updateCoords[i].y);
	}
	
	@Override
	public void reset() {
		
		super.reset();
		
		updateCoords = null;
	}
}
