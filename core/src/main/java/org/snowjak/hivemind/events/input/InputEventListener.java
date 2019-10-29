/**
 * 
 */
package org.snowjak.hivemind.events.input;

import org.snowjak.hivemind.events.input.InputEvent.MouseButton;

import com.badlogic.gdx.utils.Bits;

/**
 * Entry-point for {@link InputEvent}-handling.
 * <p>
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class InputEventListener {
	
	private final boolean isDiscrete;
	private boolean isActive = false;
	private float remainingInterval = 0f;
	
	private final Bits keys;
	private final MouseButton button;
	
	/**
	 * Construct a new InputEventListener. This Listener will be "discrete":
	 * <ul>
	 * <li>{@link #receive(InputEvent)} is called whenever the depressed key and
	 * modifier-keys match</li>
	 * </ul>
	 * 
	 * @param key
	 * @param alt
	 * @param ctrl
	 * @param shift
	 */
	public InputEventListener(GameKey key, boolean alt, boolean ctrl, boolean shift) {
		
		this(key, (alt ? GameKey.ALT_LEFT : null), (alt ? GameKey.ALT_RIGHT : null),
				(ctrl ? GameKey.CONTROL_LEFT : null), (ctrl ? GameKey.CONTROL_RIGHT : null),
				(shift ? GameKey.SHIFT_LEFT : null), (shift ? GameKey.SHIFT_RIGHT : null));
	}
	
	/**
	 * Construct a new InputEventListener. This Listener will be "discrete":
	 * <ul>
	 * <li>{@link #receive(InputEvent)} is called whenever the depressed keys
	 * match</li>
	 * </ul>
	 * 
	 * @param keys
	 */
	public InputEventListener(GameKey... keys) {
		
		this(null, keys);
	}
	
	/**
	 * Construct a new InputEventListener. This Listener will be "discrete":
	 * <ul>
	 * <li>{@link #receive(InputEvent)} is called whenever the clicked mouse-button
	 * and modifier-keys match</li>
	 * </ul>
	 * 
	 * @param button
	 * @param alt
	 * @param ctrl
	 * @param shift
	 */
	public InputEventListener(MouseButton button, boolean alt, boolean ctrl, boolean shift) {
		
		this(button, (alt ? GameKey.ALT_LEFT : null), (alt ? GameKey.ALT_RIGHT : null),
				(ctrl ? GameKey.CONTROL_LEFT : null), (ctrl ? GameKey.CONTROL_RIGHT : null),
				(shift ? GameKey.SHIFT_LEFT : null), (shift ? GameKey.SHIFT_RIGHT : null));
	}
	
	/**
	 * Construct a new InputEventListener. This Listener will be "discrete":
	 * <ul>
	 * <li>{@link #receive(InputEvent)} is called whenever the depressed keys
	 * match</li>
	 * </ul>
	 * 
	 * @param keys
	 */
	public InputEventListener(MouseButton button, GameKey... keys) {
		
		isDiscrete = true;
		
		this.keys = GameKey.get(keys);
		this.button = button;
	}
	
	/**
	 * Construct a new InputEventListener. This Listener will be "continuous":
	 * <ul>
	 * <li>{@link #receive(InputEvent)} is called with every change to the
	 * cursor-position</li>
	 * </ul>
	 * 
	 * @param alt
	 * @param ctrl
	 * @param shift
	 */
	public InputEventListener(boolean alt, boolean ctrl, boolean shift) {
		
		this.isDiscrete = false;
		this.keys = GameKey.get((alt ? GameKey.ALT_LEFT : null), (alt ? GameKey.ALT_RIGHT : null),
				(ctrl ? GameKey.CONTROL_LEFT : null), (ctrl ? GameKey.CONTROL_RIGHT : null),
				(shift ? GameKey.SHIFT_LEFT : null), (shift ? GameKey.SHIFT_RIGHT : null));
		this.button = null;
	}
	
	public Bits getKeys() {
		
		return keys;
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
	 * Called whenever this event occurs (if it is configured as "discrete") or when
	 * it is updated (if it is "continuous").
	 * 
	 * @param event
	 * @see #endReceive(InputEvent)
	 */
	public abstract void receive(InputEvent event);
	
	/**
	 * Called whenever this event <em>stops</em> -- i.e., when this
	 * InputEventListener is "deactivated".
	 * 
	 * @param event
	 */
	public void endReceive(InputEvent event) {
		
	}
}
