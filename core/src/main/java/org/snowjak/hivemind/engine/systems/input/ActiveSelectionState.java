/**
 * 
 */
package org.snowjak.hivemind.engine.systems.input;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.IsSelectable;
import org.snowjak.hivemind.engine.components.IsSelected;
import org.snowjak.hivemind.engine.systems.InputEventProcessingSystem;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;
import org.snowjak.hivemind.gamescreen.InputEventListener;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.msg.Telegram;

import squidpony.squidmath.OrderedSet;

/**
 * A possible state for the {@link InputEventProcessingSystem}. In this state,
 * the user has selected one or more entities. Possible transitions include:
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
public class ActiveSelectionState implements InputSystemState {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<IsSelectable> IS_SELECTABLE = ComponentMapper.getFor(IsSelectable.class);
	
	private final OrderedSet<Entity> selected;
	
	private final InputEventListener leftClickListener = InputEventListener.build()
	//@formatter:off
			.button(MouseButton.LEFT_BUTTON)
			.onEvent(e -> {
				final Entity screenMapEntity = Context.getEngine().getSystem(UniqueTagManager.class).get(Tags.SCREEN_MAP);
				if(screenMapEntity == null || !HAS_MAP.has(screenMapEntity))
					return;
				
				final OrderedSet<Entity> clicked = HAS_MAP.get(screenMapEntity).getEntities().getAt(e.getMapCursor());
				final OrderedSet<Entity> selected = new OrderedSet<>(clicked.size());
				for(int i=0; i<clicked.size();i++)
					if(IS_SELECTABLE.has(clicked.getAt(i)))
						selected.add(clicked.getAt(i));
				
				if(selected.isEmpty())
					removeSelection();
				else
					redoSelection(selected);
				
			})
			//@formatter:on
			.get();
	
	public ActiveSelectionState(OrderedSet<Entity> selected) {
		
		this.selected = selected;
	}
	
	@Override
	public void enter(InputEventProcessingSystem entity) {
		entity.registerListener(leftClickListener);
	}
	
	@Override
	public void update(InputEventProcessingSystem entity) {
		
		synchronized (this) {
			if (selected.isEmpty())
				entity.getStateMachine().revertToPreviousState();
		}
	}
	
	@Override
	public void exit(InputEventProcessingSystem entity) {
		
		entity.unregisterListener(leftClickListener);
	}
	
	/**
	 * When the user clears the current selection, this method resets all
	 * previously-selected Entities and arranges for control to return to the
	 * previously state.
	 */
	private void removeSelection() {
		
		synchronized (this) {
			
			for (int i = 0; i < selected.size(); i++) {
				final Entity e = selected.getAt(i);
				e.remove(IsSelected.class);
			}
			
			selected.clear();
		}
	}
	
	/**
	 * When the user redoes the current selection, this method redoes that
	 * selection.
	 * 
	 * @param selected
	 */
	private void redoSelection(OrderedSet<Entity> selected) {
		
		synchronized (this) {
			
			removeSelection();
			
			for (int i = 0; i < selected.size(); i++) {
				this.selected.add(selected.getAt(i));
				selected.getAt(i).add(Context.getEngine().createComponent(IsSelected.class));
			}
		}
	}
	
	@Override
	public boolean onMessage(InputEventProcessingSystem entity, Telegram telegram) {
		
		return false;
	}
	
}
