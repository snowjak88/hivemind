/**
 * 
 */
package org.snowjak.hivemind.gamescreen;

import java.util.EnumSet;

import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.snowjak.hivemind.events.EventPool;
import org.snowjak.hivemind.events.input.GameKey;
import org.snowjak.hivemind.events.input.InputEvent;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Bits;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedSet;

/**
 * Custom {@link InputProcessor} for {@link GameScreen}.
 * 
 * @author snowjak88
 *
 */
public class GameScreenInputProcessor extends InputAdapter implements UpdateableInputProcessor {
	
	/**
	 * Discrete {@link InputEventListener}s will be repeatedly fired every {@code N}
	 * seconds, so long as their respective key-combinations are active.
	 */
	public static final float REPEAT_INTERVAL = 0.1f;
	
	private int offsetX, offsetY;
	private float cellWidth, cellHeight, gridWidth, gridHeight;
	
	private final ScreenMapTranslator gridTranslator;
	
	private int mouseX = Integer.MIN_VALUE, mouseY = Integer.MIN_VALUE, prevMouseX, prevMouseY;
	private EnumSet<MouseButton> activeButtons = EnumSet.noneOf(MouseButton.class);
	private Bits activeKeys = new Bits(GameKey.MAX_BIT_INDEX);
	
	private final OrderedSet<InputEventListener> inputListeners = new OrderedSet<>();
	private final MutableIntSet activeInputListeners = new IntHashSet();
	
	public GameScreenInputProcessor(int offsetX, int offsetY, float cellWidth, float cellHeight, float gridWidth,
			float gridHeight, ScreenMapTranslator gridTranslator) {
		
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
		this.gridTranslator = gridTranslator;
	}
	
	public void resize(float cellWidth, float cellHeight, float gridWidth, float gridHeight, int offsetX, int offsetY) {
		
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.gridWidth = gridWidth;
		this.gridHeight = gridHeight;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		
		synchronized (this) {
			final GameKey key = GameKey.getForKey(keycode);
			if (key == null)
				return false;
			
			activeKeys.set(key.getBitIndex());
			
			checkListeners();
			
			return true;
		}
	}
	
	@Override
	public boolean keyUp(int keycode) {
		
		synchronized (this) {
			final GameKey key = GameKey.getForKey(keycode);
			if (key == null)
				return false;
			
			activeKeys.clear(key.getBitIndex());
			
			checkListeners();
			
			return true;
		}
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int buttonCode) {
		
		synchronized (this) {
			final MouseButton button = MouseButton.getFor(buttonCode);
			if (button == null)
				return false;
			
			activeButtons.add(button);
			
			checkListeners();
			
			return true;
		}
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int buttonCode) {
		
		synchronized (this) {
			final MouseButton button = MouseButton.getFor(buttonCode);
			if (button == null)
				return false;
			
			activeButtons.remove(button);
			
			checkListeners();
			
			return true;
		}
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		
		return mouseMoved(screenX, screenY);
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		
		synchronized (this) {
			prevMouseX = mouseX;
			prevMouseY = mouseY;
			
			mouseX = toGridX(screenX);
			mouseY = toGridY(screenY);
			
			if (mouseX != prevMouseX || mouseY != prevMouseY)
				checkListeners();
			
			return true;
		}
	}
	
	/**
	 * Check all registered {@link InputEventListener}s against the current
	 * input-state. Activate all listeners that match the current input-state,
	 * deactivate all listeners that don't, and dispatch the current
	 * {@link InputEvent} to {@link InputEventListener#receive(InputEvent)
	 * receive(...)} and {@link InputEventListener#endReceive(InputEvent)
	 * endReceive(...)}, as appropriate.
	 */
	protected void checkListeners() {
		
		final InputEvent event = getInputEvent();
		for (int i = 0; i < inputListeners.size(); i++) {
			
			final InputEventListener listener = inputListeners.getAt(i);
			
			checkListener(i, listener, event);
		}
		
		EventPool.get().retire(event);
	}
	
	protected void checkListener(InputEventListener listener, InputEvent event) {
		
		final int idx = inputListeners.indexOf(listener);
		if (idx > -1)
			checkListener(idx, listener, event);
	}
	
	protected void checkListener(int index, InputEventListener listener, InputEvent event) {
		
		final boolean isAlreadyActive = activeInputListeners.contains(index);
		final boolean matches = listener.matches(event);
		
		if (matches && !isAlreadyActive) {
			//
			// Activate this listener
			//
			
			activeInputListeners.add(index);
			listener.setActive(true);
			listener.receive(event);
			
			if (listener.isDiscrete())
				listener.setRemainingInterval(REPEAT_INTERVAL);
			
		} else if (!matches && isAlreadyActive) {
			//
			// Deactivate this listener
			//
			
			listener.endReceive(event);
			
			listener.setActive(false);
			activeInputListeners.remove(index);
		}
	}
	
	/**
	 * Construct an {@link InputEvent} representing the current keys, mouse-button,
	 * and cursor-position.
	 * 
	 * @return
	 */
	protected InputEvent getInputEvent() {
		
		final InputEvent event = EventPool.get().get(InputEvent.class);
		
		event.setKeys(activeKeys);
		event.setButtons(activeButtons);
		
		final Coord screenCursor = Coord.get(mouseX, mouseY);
		event.setScreenCursor(screenCursor);
		event.setMapCursor(gridTranslator.screenToMap(screenCursor));
		
		return event;
	}
	
	private int toGridX(int worldX) {
		
		return MathUtils.floor((worldX + offsetX) / cellWidth);
	}
	
	private int toGridY(int worldY) {
		
		return MathUtils.floor((worldY + offsetY) / cellHeight);
	}
	
	/**
	 * Register the given {@link InputEventListener} with this input-processor.
	 * 
	 * @param listener
	 */
	public void registerInputListener(InputEventListener listener) {
		
		synchronized (this) {
			inputListeners.add(listener);
			
			final InputEvent event = getInputEvent();
			checkListener(listener, event);
			EventPool.get().retire(event);
		}
	}
	
	/**
	 * Un-register the given {@link InputEventListener} from this input-processor.
	 * 
	 * @param listener
	 */
	public void unregisterInputListener(InputEventListener listener) {
		
		synchronized (this) {
			final int index = inputListeners.indexOf(listener);
			if (index < 0)
				return;
			
			activeInputListeners.remove(index);
			inputListeners.removeAt(index);
		}
	}
	
	@Override
	public void update(float delta) {
		
		synchronized (this) {
			updateListeners(delta);
		}
	}
	
	protected void updateListeners(float delta) {
		
		final InputEvent event = getInputEvent();
		
		final MutableIntIterator activeIterator = activeInputListeners.intIterator();
		while (activeIterator.hasNext()) {
			final InputEventListener listener = inputListeners.getAt(activeIterator.next());
			
			if (listener == null) {
				activeIterator.remove();
				continue;
			}
			
			if (listener.isDiscrete()) {
				listener.setRemainingInterval(listener.getRemainingInterval() - delta);
				if (listener.getRemainingInterval() <= 0f) {
					listener.setRemainingInterval(REPEAT_INTERVAL);
					listener.receive(event);
				}
				
			} else
				listener.receive(event);
		}
		
		EventPool.get().retire(event);
	}
}
