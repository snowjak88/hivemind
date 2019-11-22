/**
 * 
 */
package org.snowjak.hivemind.engine.systems.input;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.IsSelectableNow;
import org.snowjak.hivemind.engine.components.IsSelected;
import org.snowjak.hivemind.engine.systems.InputEventProcessingSystem;
import org.snowjak.hivemind.engine.systems.display.PsychicEnergyMapDrawingSystem;
import org.snowjak.hivemind.engine.systems.display.ScreenCenteringSystem;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.events.input.GameKey;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;
import org.snowjak.hivemind.gamescreen.InputEventListener;
import org.snowjak.hivemind.gamescreen.InputHandlers;
import org.snowjak.hivemind.gamescreen.updates.FreeLayer;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.LayerUpdate;
import org.snowjak.hivemind.util.Drawing;
import org.snowjak.hivemind.util.Drawing.BoxStyle;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.msg.Telegram;

import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

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
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<IsSelectableNow> IS_SELECTABLE_NOW = ComponentMapper
			.getFor(IsSelectableNow.class);
	
	private Coord beginSelection = null, endSelection = null;
	private boolean updateSelectionBox = false;
	private boolean clickComplete = false;
	
	private InputEventListener clickListener = null;
	
	//@formatter:off
	private static final InputEventListener backgroundListener_displayPsychic =
			InputEventListener.build()
							.one(GameKey.CONTROL_LEFT, GameKey.CONTROL_RIGHT)
							.onEvent(e -> {
								if(Context.getEngine() == null)
									return;
								final PsychicEnergyMapDrawingSystem sys = Context.getEngine().getSystem(PsychicEnergyMapDrawingSystem.class);
								if(sys == null)
									return;
								sys.activate();
							})
							.onEventEnd(e -> {
								if(Context.getEngine() == null)
									return;
								final PsychicEnergyMapDrawingSystem sys = Context.getEngine().getSystem(PsychicEnergyMapDrawingSystem.class);
								if(sys == null)
									return;
								sys.deactivate();
							})
							.get();
	private static final InputEventListener backgroundListener_centerScreenOnPlayer =
			InputEventListener.build()
							.all(GameKey.C)
							.onEventEnd(e -> {
								if(Context.getEngine() == null)
									return;
								Context.getEngine().getSystem(ScreenCenteringSystem.class).centerOnPlayer();
							})
							.get();
	//@formatter:on
	
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
		
		entity.registerListener(backgroundListener_displayPsychic);
		entity.registerListener(backgroundListener_centerScreenOnPlayer);
		
		entity.registerListener(clickListener);
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
			final Entity povEntity = entity.getEngine().getSystem(UniqueTagManager.class).get(Tags.POV);
			final HasMap povMap = HAS_MAP.get(povEntity);
			
			final int startX = (beginSelection.x > endSelection.x) ? endSelection.x : beginSelection.x;
			final int startY = (beginSelection.y > endSelection.y) ? endSelection.y : beginSelection.y;
			final int endX = (beginSelection.x < endSelection.x) ? endSelection.x : beginSelection.x;
			final int endY = (beginSelection.y < endSelection.y) ? endSelection.y : beginSelection.y;
			
			final OrderedSet<Entity> selected = new OrderedSet<>((endX - startX + 1) * (endY - startY + 1));
			
			for (int x = startX; x <= endX; x++)
				for (int y = startY; y <= endY; y++) {
					
					final OrderedSet<Entity> entitiesAt = povMap.getEntities().getAt(Coord.get(x, y));
					if (entitiesAt == null || entitiesAt.isEmpty())
						continue;
					
					for (int i = 0; i < entitiesAt.size(); i++) {
						if (IS_SELECTABLE_NOW.has(entitiesAt.getAt(i))) {
							entitiesAt.getAt(i).add(entity.getEngine().createComponent(IsSelected.class));
							selected.add(entitiesAt.getAt(i));
						}
					}
				}
				
			//
			// Change to the "active-selection" input-state if there is anything selected.
			//
			
			if (!selected.isEmpty())
				entity.getStateMachine().changeState(new ActiveSelectionState(selected));
			
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
			
			entity.unregisterListener(clickListener);
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
