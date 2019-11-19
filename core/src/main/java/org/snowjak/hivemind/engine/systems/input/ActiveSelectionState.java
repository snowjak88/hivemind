/**
 * 
 */
package org.snowjak.hivemind.engine.systems.input;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasPathfinder;
import org.snowjak.hivemind.engine.components.IsMovingTo;
import org.snowjak.hivemind.engine.components.IsSelectable;
import org.snowjak.hivemind.engine.components.IsSelected;
import org.snowjak.hivemind.engine.systems.InputEventProcessingSystem;
import org.snowjak.hivemind.engine.systems.RunnableExecutingSystem;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;
import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.gamescreen.InputEventListener;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.TempSidebarLocationUpdate;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.msg.Telegram;

import squidpony.squidai.DijkstraMap;
import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * A possible state for the {@link InputEventProcessingSystem}. In this state,
 * the user has selected one or more entities. Possible transitions include:
 * <ul>
 * <li>(right-click) -- selected Entities receive {@link IsMovingTo}
 * (destination = map-cursor), potentially triggering pathfind-and-move
 * behavior</li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
public class ActiveSelectionState implements InputSystemState {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<IsSelectable> IS_SELECTABLE = ComponentMapper.getFor(IsSelectable.class);
	
	private final OrderedSet<Entity> selected = new OrderedSet<>();
	
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
	
	private final InputEventListener rightClickListener = InputEventListener.build()
	//@formatter:off
					.button(MouseButton.RIGHT_BUTTON)
					.onEvent(e -> {
				//@formatter:on
				synchronized (ActiveSelectionState.this) {
					final Engine eng = Context.getEngine();
					final RunnableExecutingSystem res = eng.getSystem(RunnableExecutingSystem.class);
					for (int i = 0; i < selected.size(); i++) {
						final Entity entity = selected.getAt(i);
						res.submit(() -> {
							final IsMovingTo moveTo = Context.getEngine().createComponent(IsMovingTo.class);
							moveTo.setDestination(e.getMapCursor());
							entity.add(moveTo);
						});
					}
				}
				//@formatter:off
					})
					//@formatter:on
			.get();
				
	private final InputEventListener cursorLocationListener = InputEventListener.build()
	//@formatter:off
							.continuous()
							.onEvent(e -> {
								final GameScreen gs = Context.getGameScreen();
								if (gs == null)
									return;
								
								final Coord loc = e.getMapCursor();
								double cost = 0;
								
								final Entity playerEntity = Context.getEngine().getSystem(UniqueTagManager.class).get(Tags.PLAYER);
								if(playerEntity != null)
									if(ComponentMapper.getFor(HasPathfinder.class).has(playerEntity)) {
										final HasPathfinder hpf = ComponentMapper.getFor(HasPathfinder.class).get(playerEntity);
										hpf.getLock().acquireUninterruptibly();
										final DijkstraMap pf = hpf.getPathfinder();
										if(pf != null && pf.physicalMap.length > loc.x && pf.physicalMap[loc.x].length > loc.y)
											cost = pf.physicalMap[loc.x][loc.y];
										hpf.getLock().release();
									}
								
								{
								final TempSidebarLocationUpdate upd = GameScreenUpdatePool.get().get(TempSidebarLocationUpdate.class);
								upd.setLocation(e.getMapCursor());
								upd.setCost(cost);
								gs.postGameScreenUpdate(upd);
								}
							})
							//@formatter:on
			.get();
	
	public ActiveSelectionState(OrderedSet<Entity> selected) {
		
		this.selected.addAll(selected);
	}
	
	@Override
	public void enter(InputEventProcessingSystem entity) {
		
		entity.registerListener(leftClickListener);
		entity.registerListener(rightClickListener);
		entity.registerListener(cursorLocationListener);
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
		entity.unregisterListener(rightClickListener);
		entity.unregisterListener(cursorLocationListener);
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
