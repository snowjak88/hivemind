/**
 * 
 */
package org.snowjak.hivemind.engine.systems.input;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.systems.InputEventProcessingSystem;
import org.snowjak.hivemind.events.input.GameKey;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;
import org.snowjak.hivemind.gamescreen.InputEventListener;
import org.snowjak.hivemind.gamescreen.InputHandlers;
import org.snowjak.hivemind.gamescreen.updates.FreeLayer;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.LayerUpdate;
import org.snowjak.hivemind.gamescreen.updates.RegisterInputEventListener;
import org.snowjak.hivemind.gamescreen.updates.UnregisterInputEventListener;
import org.snowjak.hivemind.util.Drawing;
import org.snowjak.hivemind.util.Drawing.BoxStyle;

import com.badlogic.gdx.ai.msg.Telegram;

import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.Coord;

/**
 * Base state for the {@link InputEventProcessingSystem}. In this state, the
 * user can:
 * <ul>
 * <li>Select one or more entities --> {@link ActiveSelectionState}</li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
public class BaseInputState implements InputSystemState {
	
	private static final float SELECTION_BOX_COLOR_FLOAT = SColor.multiplyAlpha(SColor.AURORA_CLOUD, 0.5f);
	private static final String SELECTION_LAYER_NAME = BaseInputState.class.getName();
	
	private Coord beginSelection = null, endSelection = null;
	private boolean updateSelectionBox = false;
	private boolean clickComplete = false;
	
	private InputEventListener clickListener = null;
	
	@Override
	public void enter(InputEventProcessingSystem entity) {
		
		//@formatter:off
		clickListener = InputEventListener.build()
							.button(MouseButton.LEFT_BUTTON)
							.exclude(GameKey.ALT_LEFT, GameKey.ALT_RIGHT)
							.continuous()
							.handlers(InputHandlers.drag((e) -> {
								beginSelection = e.getMapCursor();
							}, (e,s,f) -> {
								endSelection = f;
								updateSelectionBox = true;
							}, (e,s,f) -> {
								endSelection = f;
								clickComplete = true;
							}))
							.get();
		//@formatter:on
		
		final RegisterInputEventListener upd = GameScreenUpdatePool.get().get(RegisterInputEventListener.class);
		upd.setListener(clickListener);
		Context.getGameScreen().postGameScreenUpdate(upd);
	}
	
	@Override
	public void update(InputEventProcessingSystem entity) {
		
		if (clickComplete) {
			
			//
			// Clear the selection-box
			//
			{
				final LayerUpdate upd = GameScreenUpdatePool.get().get(LayerUpdate.class);
				upd.setName(SELECTION_LAYER_NAME);
				upd.setProcedure((l) -> l.clear());
				Context.getGameScreen().postGameScreenUpdate(upd);
			}
			
			//
			// Identify which entities were selected.
			//
			// TODO
			
			//
			// Flag the selected entities.
			//
			// TODO
			
			//
			// Change to the "active-selection" input-state.
			//
			// TODO
			
			beginSelection = null;
			endSelection = null;
			updateSelectionBox = false;
			clickComplete = false;
			
		} else {
			
			//
			// If we haven't written the selection-box yet, or if the selection-region has
			// changed since we last wrote it, we'd better (re-)draw the selection-box.
			//
			
			if (beginSelection != null && endSelection != null && updateSelectionBox) {
				
				{
					final LayerUpdate upd = GameScreenUpdatePool.get().get(LayerUpdate.class);
					upd.setName(SELECTION_LAYER_NAME);
					upd.setProcedure((l) -> {
						
						l.clear();
						Drawing.drawBox(BoxStyle.SINGLE_LINE, l, beginSelection, endSelection,
								SELECTION_BOX_COLOR_FLOAT);
					});
					Context.getGameScreen().postGameScreenUpdate(upd);
				}
				
				//
				//
				updateSelectionBox = false;
				
			}
		}
	}
	
	@Override
	public void exit(InputEventProcessingSystem entity) {
		
		if (clickListener != null) {
			
			//
			// Get rid of the selection click-listener.
			//
			
			final UnregisterInputEventListener upd = GameScreenUpdatePool.get().get(UnregisterInputEventListener.class);
			upd.setListener(clickListener);
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
		
		if (Context.getGameScreen() != null) {
			
			//
			// Free the "selection-box" layer for other uses.
			//
			
			final FreeLayer upd = GameScreenUpdatePool.get().get(FreeLayer.class);
			upd.setName(SELECTION_LAYER_NAME);
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
	}
	
	@Override
	public boolean onMessage(InputEventProcessingSystem entity, Telegram telegram) {
		
		return false;
	}
	
}
