/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.Materials;
import org.snowjak.hivemind.Materials.Material;
import org.snowjak.hivemind.TerrainTypes;
import org.snowjak.hivemind.TerrainTypes.TerrainType;
import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.map.GameMap;
import org.snowjak.hivemind.util.ArrayUtil;

import squidpony.squidmath.GreasedRegion;

/**
 * Represents an update of the entire game-map.
 * 
 * @author snowjak88
 *
 */
public class MapUpdate implements GameScreenUpdate {
	
	protected short[][] terrain = new short[0][0], material = new short[0][0];
	
	protected GreasedRegion visible = new GreasedRegion(0, 0);
	
	public void setMap(GameMap map, GreasedRegion visible) {
		
		resize(map.getWidth(), map.getHeight());
		
		synchronized (map) {
			for (int i = 0; i < map.getWidth(); i++)
				for (int j = 0; j < map.getHeight(); j++) {
					terrain[i][j] = map.getTerrainIndex(i, j);
					material[i][j] = map.getMaterialIndex(i, j);
				}
		}
		
		this.visible.remake(visible);
	}
	
	protected void resize(int width, int height) {
		
		if (terrain.length != width || terrain[0].length != height)
			terrain = new short[width][height];
		if (material.length != width || material[0].length != height)
			material = new short[width][height];
		
		visible.resizeAndEmpty(width, height);
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		for (int x = 0; x < terrain.length; x++)
			for (int y = 0; y < terrain[x].length; y++)
				drawCell(gameScreen, x, y);
	}
	
	protected void drawCell(GameScreen gameScreen, int x, int y) {
		
		final TerrainType tt = TerrainTypes.get().getAt(terrain[x][y]);
		final Material mat = Materials.get().get(material[x][y]);
		if (tt != null && mat != null)
			gameScreen.getMapSurface().putWithReverseLight(x, y, tt.getCh(), tt.getForegroundFloat(), mat.getColorFloat(),
					GameScreen.NOT_VISIBLE_DARKNESS_FLOAT, (visible.contains(x, y)) ? 0f : 1f);
	}
	
	@Override
	public void reset() {
		
		ArrayUtil.fill(terrain, (short) -1);
		ArrayUtil.fill(material, (short) -1);
		visible.clear();
	}
}
