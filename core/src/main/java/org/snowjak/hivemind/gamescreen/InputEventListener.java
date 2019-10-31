/**
 * 
 */
package org.snowjak.hivemind.gamescreen;

import java.util.function.Consumer;

import org.snowjak.hivemind.events.input.GameKey;
import org.snowjak.hivemind.events.input.InputEvent;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;

import com.badlogic.gdx.utils.Bits;

import squidpony.squidmath.Coord;

/**
 * Entry-point for {@link InputEvent}-handling.
 * <h3>Continuous / Discrete</h3>
 * <p>
 * An InputEventListener may be classed as either <strong>discrete</strong>, or
 * <strong>continuous</strong>.
 * </p>
 * <h4>Discrete</h4> The InputEventListener will receive only two events:
 * <p>
 * <ul>
 * <li>{@link #receive(InputEvent)}, when the event starts</li>
 * <li>{@link #endReceive(InputEvent)}, when the event finishes</li>
 * </ul>
 * Typically, discrete events will correspond to key-presses, mouse-clicks, and
 * other "one-time" actions. <strong>Note</strong> that discrete events
 * <em>will</em> fire repeatedly, at an interval controlled by the
 * {@link GameScreenInputProcessor}, for as long as the event-condition is
 * satisfied.
 * </p>
 * <h4>Continuous</h4> The InputEventListener will receive constant updates
 * across the lifetime of the event:
 * <p>
 * <ul>
 * <li>{@link #receive(InputEvent)}, with every frame-update</li>
 * <li>{@link #endReceive(InputEvent)}, when the event finishes</li>
 * </ul>
 * Typically, you will use a continuous event to track the cursor-position or
 * handle the user holding down a key-combination
 * </p>
 * 
 * @author snowjak88
 *
 */
public class InputEventListener {
	
	private boolean isDiscrete = true;
	private boolean isActive = false;
	private float remainingInterval = 0f;
	
	private Coord windowStart = null, windowEnd = null;
	
	private Bits allKeys = new Bits(), oneOfKeys = new Bits(), excludeKeys = new Bits();
	private MouseButton button = null;
	private Consumer<InputEvent> onEvent = null;
	private Consumer<InputEvent> onEventEnd = null;
	
	/**
	 * Start building a new {@link InputEventListener}.
	 * 
	 * @return the new {@link Builder}
	 */
	public static InputEventListener.Builder build() {
		
		return new Builder(new InputEventListener());
	}
	
	private InputEventListener() {
		
	}
	
	public Coord getWindowStart() {
		
		return windowStart;
	}
	
	public Coord getWindowEnd() {
		
		return windowEnd;
	}
	
	public Bits getAllKeys() {
		
		return allKeys;
	}
	
	public Bits getOneOfKeys() {
		
		return oneOfKeys;
	}
	
	public Bits getExcludeKeys() {
		
		return excludeKeys;
	}
	
	public MouseButton getButton() {
		
		return button;
	}
	
	public boolean isDiscrete() {
		
		return isDiscrete;
	}
	
	public boolean isActive() {
		
		return isActive;
	}
	
	public void setActive(boolean isActive) {
		
		this.isActive = isActive;
	}
	
	public float getRemainingInterval() {
		
		return remainingInterval;
	}
	
	public void setRemainingInterval(float remainingInterval) {
		
		this.remainingInterval = remainingInterval;
	}
	
	/**
	 * Determine if this InputEventListener can receive the given InputEvent --
	 * i.e., the configured {@link MouseButton} (if any) matches that of the
	 * InputEvent, and the InputEvent's keys include (but are not necessarily
	 * limited to) this listener's configured keys.
	 * 
	 * @param event
	 * @return
	 */
	public boolean matches(InputEvent event) {
		
		//@formatter:off
		return ((this.button == null || event.getButtons().contains(this.button))
			&& (this.windowStart == null || (event.getScreenCursor().x >= this.windowStart.x && event.getScreenCursor().y >= this.windowStart.y))
			&& (this.windowEnd == null || (event.getScreenCursor().x <= this.windowEnd.x && event.getScreenCursor().y <= this.windowEnd.y))
			&& (this.allKeys.isEmpty() || event.getKeys().containsAll(this.allKeys))
			&& (this.oneOfKeys.isEmpty() || event.getKeys().intersects(oneOfKeys))
			&& (this.excludeKeys.isEmpty() || !event.getKeys().intersects(excludeKeys)));
		//@formatter:on
	}
	
	/**
	 * Called whenever this event occurs (if it is configured as "discrete") or when
	 * it is updated (if it is "continuous").
	 * 
	 * @param event
	 * @see #endReceive(InputEvent)
	 */
	public void receive(InputEvent event) {
		
		if (onEvent != null)
			onEvent.accept(event);
	}
	
	/**
	 * Called whenever this event <em>stops</em> -- i.e., when this
	 * InputEventListener is "deactivated".
	 * 
	 * @param event
	 */
	public void endReceive(InputEvent event) {
		
		if (onEventEnd != null)
			onEventEnd.accept(event);
	}
	
	/**
	 * Interface providing both the "ongoing" and "ending"
	 * {@link InputEvent}-handlers at once.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class InputEventHandlers {
		
		private final Consumer<InputEvent> eventHandler, endEventHandler;
		
		public InputEventHandlers(Consumer<InputEvent> eventHandler, Consumer<InputEvent> endEventHandler) {
			
			this.eventHandler = eventHandler;
			this.endEventHandler = endEventHandler;
		}
		
		public Consumer<InputEvent> getEventHandler() {
			
			return eventHandler;
		}
		
		public Consumer<InputEvent> getEndEventHandler() {
			
			return endEventHandler;
		}
	}
	
	public static class Builder {
		
		private final InputEventListener listener;
		
		private Builder(InputEventListener listener) {
			
			this.listener = listener;
		}
		
		/**
		 * Incoming {@link InputEvent}s must have <em>all</em> the given {@link GameKey
		 * keys} for this listener to be activated. (Overrides any previously-specified
		 * {@code all(...)} keys.)
		 * 
		 * @param keys
		 * @return
		 */
		public Builder all(GameKey... keys) {
			
			listener.allKeys = GameKey.getBits(keys);
			return this;
		}
		
		/**
		 * Incoming {@link InputEvent}s must have <em>at least one</em> of the given
		 * {@link GameKey keys} for this listener to be activated. (Overrides any
		 * previously-specified {@code one(...)} keys.)
		 * 
		 * @param keys
		 * @return
		 */
		public Builder one(GameKey... keys) {
			
			listener.oneOfKeys = GameKey.getBits(keys);
			return this;
		}
		
		/**
		 * Incoming {@link InputEvent}s must have <em>none</em> the given {@link GameKey
		 * keys} for this listener to be activated. (Overrides any previously-specified
		 * {@code exclude(...)} keys.)
		 * 
		 * @param keys
		 * @return
		 */
		public Builder exclude(GameKey... keys) {
			
			listener.excludeKeys = GameKey.getBits(keys);
			return this;
		}
		
		/**
		 * Incoming {@link InputEvent}s must include the given {@link MouseButton} for
		 * this listener to be activated. (Overrides any previously-specified
		 * {@code button(...)} keys.)
		 * 
		 * @param button
		 * @return
		 */
		public Builder button(MouseButton button) {
			
			listener.button = button;
			return this;
		}
		
		/**
		 * Indicates that this listener will be "continuous" -- i.e., it will receive
		 * InputHandlers continuously every time the active-keys or cursor-position
		 * change.
		 * 
		 * @return
		 */
		public Builder continuous() {
			
			return continuous(true);
		}
		
		/**
		 * Sets whether or not this listener will be "continuous" -- i.e., it will
		 * receive InputHandlers continuously every time the active-keys or
		 * cursor-position change.
		 * 
		 * @param isContinuous
		 * @return
		 */
		public Builder continuous(boolean isContinuous) {
			
			listener.isDiscrete = !isContinuous;
			return this;
		}
		
		/**
		 * Sets the window (in screen-grid coordinates) that the mouse-cursor must be
		 * within for an {@link InputEvent} to be accepted by this listener.
		 * 
		 * @param start
		 * @param end
		 * @return
		 */
		public Builder inWindow(Coord start, Coord end) {
			
			listener.windowStart = (start.x < end.x || start.y < end.y) ? start : end;
			listener.windowEnd = (start.x < end.x || start.y < end.y) ? end : start;
			return this;
		}
		
		/**
		 * Set the handler that should handle all incoming {@link InputEvent}s while
		 * this listener is active.
		 * 
		 * @param handler
		 * @return
		 */
		public Builder onEvent(Consumer<InputEvent> handler) {
			
			listener.onEvent = handler;
			return this;
		}
		
		/**
		 * Set the handler that should handle the {@link InputEvent} sent to this
		 * listener whenever it is deactivated.
		 * 
		 * @param handler
		 * @return
		 */
		public Builder onEventEnd(Consumer<InputEvent> handler) {
			
			listener.onEventEnd = handler;
			return this;
		}
		
		/**
		 * Set both the active- and end-event handlers at once -- e.g., using one of the
		 * functions in {@link InputHandlers}.
		 * 
		 * @param handlers
		 * @return
		 * @see #onEvent(Consumer)
		 * @see #onEventEnd(Consumer)
		 */
		public Builder handlers(InputEventHandlers handlers) {
			
			listener.onEvent = handlers.getEventHandler();
			listener.onEventEnd = handlers.getEndEventHandler();
			return this;
		}
		
		/**
		 * Get the constructed {@link InputEventListener} instance.
		 * 
		 * @return
		 */
		public InputEventListener get() {
			
			return listener;
		}
	}
}
