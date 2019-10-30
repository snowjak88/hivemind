/**
 * 
 */
package org.snowjak.hivemind.gamescreen;

import org.snowjak.hivemind.events.input.GameKey;
import org.snowjak.hivemind.events.input.InputEvent;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;

import com.badlogic.gdx.utils.Bits;

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
	
	private Bits allKeys = new Bits(), oneOfKeys = new Bits(), excludeKeys = new Bits();
	private MouseButton button = null;
	private EventHandler onEvent = null;
	private EndEventHandler onEventEnd = null;
	
	public static InputEventListener.Builder build() {
		
		return new Builder(new InputEventListener());
	}
	
	private InputEventListener() {
		
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
		
		return ((this.button == null || event.getButtons().contains(this.button))
				&& (this.allKeys.isEmpty() || event.getKeys().containsAll(this.allKeys))
				&& (this.oneOfKeys.isEmpty() || event.getKeys().intersects(oneOfKeys))
				&& (this.excludeKeys.isEmpty() || !event.getKeys().intersects(excludeKeys)));
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
			onEvent.receive(event);
	}
	
	/**
	 * Called whenever this event <em>stops</em> -- i.e., when this
	 * InputEventListener is "deactivated".
	 * 
	 * @param event
	 */
	public void endReceive(InputEvent event) {
		
		if (onEventEnd != null)
			onEventEnd.endReceive(event);
	}
	
	@FunctionalInterface
	public interface EventHandler {
		
		/**
		 * Called whenever this event occurs (if it is configured as "discrete") or when
		 * it is updated (if it is "continuous").
		 * 
		 * @param event
		 */
		public void receive(InputEvent event);
	}
	
	@FunctionalInterface
	public interface EndEventHandler {
		
		/**
		 * Called whenever this event <em>stops</em> -- i.e., when this
		 * InputEventListener is "deactivated".
		 * 
		 * @param event
		 */
		public void endReceive(InputEvent event);
	}
	
	public static class Builder {
		
		private final InputEventListener listener;
		
		private Builder(InputEventListener listener) {
			
			this.listener = listener;
		}
		
		public Builder all(GameKey... keys) {
			
			listener.allKeys = GameKey.getBits(keys);
			return this;
		}
		
		public Builder one(GameKey... keys) {
			
			listener.oneOfKeys = GameKey.getBits(keys);
			return this;
		}
		
		public Builder exclude(GameKey... keys) {
			
			listener.excludeKeys = GameKey.getBits(keys);
			return this;
		}
		
		/**
		 * Configure the InputEventListener to trigger when the given
		 * {@link MouseButton} is pressed.
		 * 
		 * @param button
		 * @return
		 */
		public Builder button(MouseButton button) {
			
			listener.button = button;
			return this;
		}
		
		public Builder continuous() {
			
			return continuous(true);
		}
		
		public Builder continuous(boolean isContinuous) {
			
			listener.isDiscrete = !isContinuous;
			return this;
		}
		
		public Builder onEvent(EventHandler handler) {
			
			listener.onEvent = handler;
			return this;
		}
		
		public Builder onEventEnd(EndEventHandler handler) {
			
			listener.onEventEnd = handler;
			return this;
		}
		
		public InputEventListener get() {
			
			return listener;
		}
	}
}
