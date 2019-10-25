/**
 * 
 */
package org.snowjak.hivemind.events.input;

import com.badlogic.gdx.InputAdapter;

/**
 * Extension of {@link InputAdapter} to incorporate
 * {@link UpdateableInputProcessor}
 * 
 * @author snowjak88
 *
 */
public abstract class UpdateableInputAdapter extends InputAdapter implements UpdateableInputProcessor {
	
	@Override
	public void update(float delta) {
		
	}
	
}
