/**
 * 
 */
package org.snowjak.hivemind.ui;

import org.snowjak.hivemind.App;
import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.display.Fonts;

import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.TextCellFactory;

/**
 * Presents the game-map, encapsulated as a {@link WidgetGroup} for inclusion in
 * the scene-graph.
 * 
 * @author snowjak88
 *
 */
public class GameScreen extends WidgetGroup {
	
	public static final String PREFERENCE_CELL_WIDTH = "game-screen.cell-width",
			PREFERENCE_CELL_HEIGHT = "game-screen.cell-height";
	{
		Config.get().register(PREFERENCE_CELL_WIDTH, "Width (in pixels) of each game-map cell", 16f, true);
		Config.get().register(PREFERENCE_CELL_HEIGHT, "Height (in pixels) of each game-map cell", 16f, true);
	}
	
	private SparseLayers sparseLayers;
	
	public GameScreen() {
		
		super();
		
		final float cellWidth = Config.get().getFloat(PREFERENCE_CELL_WIDTH);
		final float cellHeight = Config.get().getFloat(PREFERENCE_CELL_HEIGHT);
		final int gridWidth = (int) ((float) Config.get().getInt(App.PREFERENCE_WINDOW_WIDTH) / cellWidth);
		final int gridHeight = (int) ((float) Config.get().getInt(App.PREFERENCE_WINDOW_HEIGHT) / cellHeight);
		final TextCellFactory font = new TextCellFactory().font(Fonts.get().get(Fonts.FONT_MAP));
		
		this.sparseLayers = new SparseLayers(gridWidth, gridHeight, cellWidth, cellHeight, font);
		
		this.addActor(sparseLayers);
	}
}
