/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.ui.gamescreen.GameScreen;
import org.snowjak.hivemind.ui.gamescreen.updates.ClearMapUpdate;
import org.snowjak.hivemind.ui.gamescreen.updates.DrawMapCellUpdate;
import org.snowjak.hivemind.ui.gamescreen.updates.GameScreenUpdate;
import org.snowjak.hivemind.ui.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.ui.gamescreen.updates.MapScreenSizeUpdate;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.Coord;

/**
 * This system will send {@link GameScreenUpdate}s to the {@link GameScreen}, if
 * there is an {@link Entity} tagged with {@link Tags#SCREEN_MAP} that
 * {@link HasMap has an associated GameMap}.
 * 
 * TODO: still have to figure out how to update displayed {@link Glyph}s.
 * 
 * @author snowjak88
 *
 */
public class GameScreenUpdatingSystem extends EntitySystem {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	@Override
	public void update(float deltaTime) {
		
		final UniqueTagManager utm = getEngine().getSystem(UniqueTagManager.class);
		if (!utm.has(Tags.SCREEN_MAP))
			return;
		
		final Entity e = utm.get(Tags.SCREEN_MAP);
		
		//
		// If the tagged Entity has no associated map, then just clear the screen.
		if (!HAS_MAP.has(e)) {
			GameScreen.get().postGameScreenUpdate(GameScreenUpdatePool.get().get(ClearMapUpdate.class));
			
			return;
		}
		
		final HasMap hm = HAS_MAP.get(e);
		
		//
		// If the HasMap has no updated locations, then there's nothing to do.
		if (hm.getUpdatedLocations() == null || hm.getUpdatedLocations().isEmpty())
			return;
			
		//
		// If the HasMap has no good GameMap, then there's nothing to do.
		if (hm.getMap() == null)
			return;
			
		//
		// Now -- does the GameScreen need to be resized?
		if (GameScreen.get().getGridWidth() != hm.getMap().getWidth()
				|| GameScreen.get().getGridHeight() != hm.getMap().getHeight()) {
			final MapScreenSizeUpdate upd = GameScreenUpdatePool.get().get(MapScreenSizeUpdate.class);
			upd.setWidth(hm.getMap().getWidth());
			upd.setHeight(hm.getMap().getHeight());
			GameScreen.get().postGameScreenUpdate(upd);
		}
		
		//
		// Add updates for all recently-updated locations.
		final Coord[] updatedLocations = hm.getUpdatedLocations().asCoords();
		for (int i = 0; i < updatedLocations.length; i++) {
			final DrawMapCellUpdate upd = GameScreenUpdatePool.get().get(DrawMapCellUpdate.class);
			upd.setX(updatedLocations[i].x);
			upd.setY(updatedLocations[i].y);
			upd.setCh(hm.getMap().getChar(updatedLocations[i]));
			upd.setForeground(hm.getMap().getForeground(updatedLocations[i]));
			upd.setBackground(hm.getMap().getBackground(updatedLocations[i]));
			GameScreen.get().postGameScreenUpdate(upd);
		}
		
		//
		// Now that we've queued up all updates, reset that list of updated locations.
		hm.getUpdatedLocations().clear();
	}
}
