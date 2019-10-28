/**
 * 
 */
package org.snowjak.hivemind.events.input;

import java.util.EnumSet;

import org.eclipse.collections.api.iterator.IntIterator;
import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.list.primitive.MutableBooleanList;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.snowjak.hivemind.events.EventPool;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;
import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.gamescreen.ScreenMapTranslator;
import org.snowjak.hivemind.ui.MouseHoverListener;
import org.snowjak.hivemind.ui.MouseHoverListener.MouseHoverListenerRegistrar;

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
public class GameScreenInputProcessor extends InputAdapter
		implements UpdateableInputProcessor, MouseHoverListenerRegistrar {
	
	/**
	 * Discrete {@link InputEventListener}s will be repeatedly fired every {@code N}
	 * seconds, so long as their respective key-combinations are active.
	 */
	public static final float REPEAT_INTERVAL = 0.2f;
	
	private int offsetX, offsetY;
	private float cellWidth, cellHeight, gridWidth, gridHeight;
	
	private final ScreenMapTranslator gridTranslator;
	
	private int mouseX = Integer.MIN_VALUE, mouseY = Integer.MIN_VALUE, prevMouseX, prevMouseY;
	private EnumSet<MouseButton> activeButtons = EnumSet.noneOf(MouseButton.class);
	private Bits activeKeys = new Bits(GameKey.MAX_BIT_INDEX);
	
	private final OrderedSet<InputEventListener> inputListeners = new OrderedSet<>();
	private final MutableIntSet activeInputListeners = new IntHashSet(), activeContinuousListeners = new IntHashSet();
	
	private final OrderedSet<MouseHoverListener> hoverListeners = new OrderedSet<>();
	private final MutableBooleanList activeHoverListeners = new BooleanArrayList();
	
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
			
			activateListeners();
			
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
			
			deactivateListeners();
			
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
			
			activateListeners();
			
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
			
			deactivateListeners();
			
			return true;
		}
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		
		synchronized (this) {
			prevMouseX = mouseX;
			prevMouseY = mouseY;
			
			mouseX = toGridX(screenX);
			mouseY = toGridY(screenY);
			
			if (mouseX != prevMouseX || mouseY != prevMouseY)
				updateContinuousListeners();
			
			return true;
		}
	}
	
	protected void activateListeners() {
		
		for (int i = 0; i < inputListeners.size(); i++) {
			
			if (activeInputListeners.contains(i))
				continue;
			
			final InputEventListener listener = inputListeners.getAt(i);
			if (activeKeys.containsAll(listener.getKeys())
					&& (listener.getButton() == null || activeButtons.contains(listener.getButton()))) {
				
				activeInputListeners.add(i);
				
				if (listener.isDiscrete()) {
					listener.setRemainingInterval(REPEAT_INTERVAL);
					fireListener(listener);
				} else
					activeContinuousListeners.add(i);
			}
		}
	}
	
	protected void updateContinuousListeners() {
		
		final IntIterator activeIterator = activeContinuousListeners.intIterator();
		while (activeIterator.hasNext()) {
			
			final InputEventListener listener = inputListeners.getAt(activeIterator.next());
			
			if (!listener.isDiscrete())
				fireListener(listener);
			
		}
	}
	
	protected void deactivateListeners() {
		
		final MutableIntIterator activeIterator = activeInputListeners.intIterator();
		while (activeIterator.hasNext()) {
			
			final int index = activeIterator.next();
			final InputEventListener listener = inputListeners.getAt(index);
			
			if (!activeKeys.containsAll(listener.getKeys())) {
				activeContinuousListeners.remove(index);
				activeIterator.remove();
			}
		}
		
	}
	
	protected void fireListener(InputEventListener listener) {
		
		final InputEvent event = EventPool.get().get(InputEvent.class);
		event.setKeys(activeKeys);
		event.setButton(listener.getButton());
		
		final Coord screenCursor = Coord.get(mouseX, mouseY);
		event.setScreenCursor(screenCursor);
		event.setMapCursor(gridTranslator.screenToMap(screenCursor));
		
		listener.receive(event);
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
			activeContinuousListeners.remove(index);
			inputListeners.removeAt(index);
		}
	}
	
	@Override
	public MouseHoverListener registerHoverListener(int startX, int startY, int endX, int endY,
			MouseHoverListener.MouseHoverReceiver receiver) {
		
		synchronized (this) {
			final MouseHoverListener listener = new MouseHoverListener(startX, startY, endX, endY, receiver);
			hoverListeners.add(listener);
			activeHoverListeners.add(false);
			return listener;
		}
	}
	
	@Override
	public MouseHoverListener registerHoverListener(int startX, int startY, int endX, int endY, float duration,
			MouseHoverListener.MouseHoverReceiver receiver) {
		
		synchronized (this) {
			final MouseHoverListener listener = new MouseHoverListener(startX, startY, endX, endY, duration, receiver);
			hoverListeners.add(listener);
			activeHoverListeners.add(false);
			return listener;
		}
	}
	
	@Override
	public void unregisterHoverListener(MouseHoverListener listener) {
		
		synchronized (this) {
			final int index = hoverListeners.indexOf(listener);
			if (index < 0)
				return;
			
			hoverListeners.removeAt(index);
			activeHoverListeners.removeAtIndex(index);
		}
	}
	
	@Override
	public void update(float delta) {
		
		synchronized (this) {
			updateHoverListeners(delta);
			updateDiscreteListeners(delta);
		}
	}
	
	protected void updateHoverListeners(float delta) {
		
		for (int i = 0; i < hoverListeners.size(); i++) {
			final MouseHoverListener hover = hoverListeners.getAt(i);
			
			final boolean isActive = hover.isActive(mouseX, mouseY);
			final boolean prevActive = activeHoverListeners.get(i);
			if (prevActive ^ isActive) {
				hover.reset();
				activeHoverListeners.set(i, isActive);
			}
			
			if (isActive)
				if (hover.isSatisfied(mouseX, mouseY, delta))
					hover.call(mouseX, mouseY);
		}
	}
	
	protected void updateDiscreteListeners(float delta) {
		
		final IntIterator activeIterator = activeInputListeners.intIterator();
		while (activeIterator.hasNext()) {
			final InputEventListener listener = inputListeners.getAt(activeIterator.next());
			if (listener.isDiscrete()) {
				listener.setRemainingInterval(listener.getRemainingInterval() - delta);
				if (listener.getRemainingInterval() <= 0f) {
					listener.setRemainingInterval(REPEAT_INTERVAL);
					fireListener(listener);
				}
			}
		}
	}
	
}
