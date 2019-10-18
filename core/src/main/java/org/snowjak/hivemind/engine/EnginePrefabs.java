/**
 * 
 */
package org.snowjak.hivemind.engine;

import org.snowjak.hivemind.engine.systems.RunnableExecutingSystem;
import org.snowjak.hivemind.ui.gamescreen.GameScreen;
import org.snowjak.hivemind.ui.gamescreen.updates.DrawMapCellUpdate;
import org.snowjak.hivemind.ui.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.ui.gamescreen.updates.SetMapSizeUpdate;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * Holds entity-world prefabs in code.
 * 
 * @author snowjak88
 *
 */
public class EnginePrefabs {
	
	public static void loadTest() {
		
		final Engine e = Engine.get();
		e.clear();
		
		final RunnableExecutingSystem res = e.getSystem(RunnableExecutingSystem.class);
		
		res.submit(() -> GameScreen.get().postGameScreenUpdate(new SetMapSizeUpdate(64, 64)));
		
		res.submit(() -> {
			for (int x = 0; x < 64; x++) {
				final DrawMapCellUpdate upd1 = GameScreenUpdatePool.get().get(DrawMapCellUpdate.class),
						upd2 = GameScreenUpdatePool.get().get(DrawMapCellUpdate.class);
				
				upd1.setX(x);
				upd1.setY(0);
				upd1.setCh('#');
				upd1.setForeground(SColor.WHITE);
				
				upd2.setX(x);
				upd2.setY(63);
				upd2.setCh('#');
				upd2.setForeground(SColor.WHITE);
				
				GameScreen.get().postGameScreenUpdate(upd1);
				GameScreen.get().postGameScreenUpdate(upd2);
			}
		});
		res.submit(() -> {
			for (int y = 0; y < 64; y++) {
				final DrawMapCellUpdate upd1 = GameScreenUpdatePool.get().get(DrawMapCellUpdate.class),
						upd2 = GameScreenUpdatePool.get().get(DrawMapCellUpdate.class);
				
				upd1.setX(0);
				upd1.setY(y);
				upd1.setCh('#');
				upd1.setForeground(SColor.WHITE);
				
				upd2.setX(63);
				upd2.setY(y);
				upd2.setCh('#');
				upd2.setForeground(SColor.WHITE);
				
				GameScreen.get().postGameScreenUpdate(upd1);
				GameScreen.get().postGameScreenUpdate(upd2);
			}
		});
	}
}
