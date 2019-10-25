/**
 * 
 */
package org.snowjak.hivemind.events.input;

import com.badlogic.gdx.InputProcessor;

/**
 * An extention of {@link InputProcessor} that contains some logic which must be
 * updated with every frame.
 * 
 * @author snowjak88
 *
 */
public interface UpdateableInputProcessor extends InputProcessor {
	
	/**
	 * Update this {@link InputProcessor}.
	 * 
	 * @param delta
	 *            time (in seconds) since the last frame
	 */
	public void update(float delta);
}
