/**
 * 
 */
package org.snowjak.hivemind.ui;

/**
 * A listener waiting to receive mouse-hovers.
 * 
 * @author snowjak88
 *
 */
public class MouseHoverListener {
	
	private final boolean hasDuration;
	private final float duration;
	private final int startX, startY, endX, endY;
	private final MouseHoverReceiver receiver;
	
	private float currentDuration = 0;
	
	/**
	 * Construct a new {@link MouseHoverListener} that can fire its
	 * {@link MouseHoverReceiver receiver} immediately.
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param receiver
	 */
	public MouseHoverListener(int startX, int startY, int endX, int endY, MouseHoverReceiver receiver) {
		
		this(startX, startY, endX, endY, 0, false, receiver);
	}
	
	/**
	 * Construct a new {@link MouseHoverListener} that will wait to fire its
	 * {@link MouseHoverReceiver receiver} until it's been active for
	 * {@code duration} seconds.
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param duration
	 * @param receiver
	 */
	public MouseHoverListener(int startX, int startY, int endX, int endY, float duration, MouseHoverReceiver receiver) {
		
		this(startX, startY, endX, endY, duration, (duration > 0), receiver);
	}
	
	private MouseHoverListener(int startX, int startY, int endX, int endY, float duration, boolean hasDuration,
			MouseHoverReceiver receiver) {
		
		assert (hasDuration == (duration > 0));
		assert (receiver != null);
		assert (startX <= endX);
		assert (startY <= endY);
		
		this.hasDuration = hasDuration;
		this.duration = duration;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.receiver = receiver;
	}
	
	/**
	 * Should this MouseHoverListener be activated?
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isActive(int x, int y) {
		
		return !(x < startX || y < startY || x > endX || y > endY);
	}
	
	/**
	 * Given that this {@link MouseHoverListener} {@link #isActive(int, int) is
	 * active} -- is it satisfied?
	 * <p>
	 * If this {@link MouseHoverListener} has no configured duration, this always
	 * returns {@code true}. Otherwise, this adds {@code delta} to the
	 * current-duration and returns {@code true} if the configured duration has been
	 * met.
	 * </p>
	 * 
	 * @param x
	 * @param y
	 * @param delta
	 * @return
	 */
	public boolean isSatisfied(int x, int y, float delta) {
		
		if (hasDuration) {
			currentDuration += delta;
			if (currentDuration < duration)
				return false;
		}
		
		return true;
	}
	
	/**
	 * Reset this MouseHoverListener's "current hover-duration".
	 */
	public void reset() {
		
		currentDuration = 0;
	}
	
	/**
	 * Call this {@link MouseHoverListener}'s configured receiver with the given
	 * mouse X/Y.
	 * 
	 * @param x
	 * @param y
	 */
	public void call(int x, int y) {
		
		receiver.receive(x, y);
	}
	
	/**
	 * Receiver for mouse-hovers.
	 * 
	 * @author snowjak88
	 *
	 */
	@FunctionalInterface
	public interface MouseHoverReceiver {
		
		/**
		 * Receive a mouse-hover at the given screen-location.
		 * 
		 * @param x
		 * @param y
		 */
		public void receive(int x, int y);
	}
	
	/**
	 * Provides the ability to register and un-register {@link MouseHoverListener}s.
	 * 
	 * @author snowjak88
	 *
	 */
	public interface MouseHoverListenerRegistrar {
		
		/**
		 * Register a new duration-less {@link MouseHoverListener}.
		 * 
		 * @param startX
		 * @param startY
		 * @param endX
		 * @param endY
		 * @param receiver
		 * @return the configured {@link MouseHoverListener}
		 */
		MouseHoverListener registerHoverListener(int startX, int startY, int endX, int endY,
				MouseHoverListener.MouseHoverReceiver receiver);
		
		/**
		 * Register a new {@link MouseHoverListener} that will fire only after the mouse
		 * has been in the given zone for {@code duration} seconds.
		 * 
		 * @param startX
		 * @param startY
		 * @param endX
		 * @param endY
		 * @param duration
		 * @param receiver
		 * @return
		 */
		MouseHoverListener registerHoverListener(int startX, int startY, int endX, int endY, float duration,
				MouseHoverListener.MouseHoverReceiver receiver);
		
		/**
		 * Un-register the given {@link MouseHoverListener}.
		 * 
		 * @param listener
		 */
		void unregisterHoverListener(MouseHoverListener listener);
	}
}
