/**
 * 
 */
package org.snowjak.hivemind;

/**
 * Holds the current {@link Speed clock-speed} and "is-paused" status.
 * 
 * @author snowjak88
 *
 */
public class ClockControl {
	
	private Speed speed = Speed.ONE;
	private boolean paused = false;
	
	public float getMultiplier() {
		
		synchronized (this) {
			return speed.getMultiplier();
		}
	}
	
	public Speed getSpeed() {
		
		synchronized (this) {
			return speed;
		}
	}
	
	public void setSpeed(Speed speed) {
		
		synchronized (this) {
			this.speed = speed;
		}
	}
	
	/**
	 * Switch the clock to a slower speed, if one is available.
	 */
	public void slower() {
		
		synchronized (this) {
			this.speed = this.speed.getSlower();
		}
	}
	
	/**
	 * Switch the clock to a faster speed, if one is available.
	 */
	public void faster() {
		
		synchronized (this) {
			this.speed = this.speed.getFaster();
		}
	}
	
	public boolean isPaused() {
		
		synchronized (this) {
			return paused;
		}
	}
	
	public void setPaused(boolean paused) {
		
		synchronized (this) {
			this.paused = paused;
		}
	}
	
	public void togglePaused() {
		
		synchronized (this) {
			this.paused = !this.paused;
		}
	}
	
	/**
	 * Describes the current clock-speed. There are only a fixed number of
	 * clock-speeds available, and a definite relation between them -- e.g., there
	 * are only so many times you can call {@link #getSlower()} before you start
	 * getting the same Speed instance as you had before.
	 * 
	 * @author snowjak88
	 *
	 */
	public enum Speed {
		EIGHTH(0.125f, 0, 1),
		QUARTER(0.25f, 0, 2),
		HALF(0.5f, 1, 3),
		ONE(1.0f, 2, 4),
		DOUBLE(2.0f, 3, 5),
		QUADRUPLE(4.0f, 4, 6),
		OCTUPLE(8.0f, 5, 6);
		
		private final float multiplier;
		private final int slower, faster;
		
		private Speed(float multiplier, int slower, int faster) {
			
			this.multiplier = multiplier;
			this.slower = slower;
			this.faster = faster;
		}
		
		public float getMultiplier() {
			
			return multiplier;
		}
		
		public Speed getSlower() {
			
			return Speed.values()[slower];
		}
		
		public Speed getFaster() {
			
			return Speed.values()[faster];
		}
	}
}
