/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import org.snowjak.hivemind.gamescreen.GameScreen;

import com.badlogic.gdx.graphics.Color;

import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidmath.Coord;

/**
 * @author snowjak88
 *
 */
public class TempSidebarLocationUpdate implements GameScreenUpdate {
	
	private Coord location = null;
	private double cost = 0;
	
	public Coord getLocation() {
		
		return location;
	}
	
	public void setLocation(Coord location) {
		
		this.location = location;
	}
	
	public double getCost() {
		
		return cost;
	}
	
	public void setCost(double cost) {
		
		this.cost = cost;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		final SparseLayers sidebar = gameScreen.getSidebarSurface();
		
		for (int x = 0; x < sidebar.getGridWidth(); x++) {
			sidebar.clear(x, 0);
			sidebar.clear(x, 1);
		}
		
		sidebar.put(0, 0, "Location: " + ((location == null) ? "(null)" : ("(" + location.x + "," + location.y + ")")),
				Color.WHITE, null);
		sidebar.put(0, 1, "Cost: " + cost, Color.WHITE, null);
	}
	
	@Override
	public void reset() {
		
		location = null;
		cost = 0;
	}
}
