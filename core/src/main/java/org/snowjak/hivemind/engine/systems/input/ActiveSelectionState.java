/**
 * 
 */
package org.snowjak.hivemind.engine.systems.input;

import org.snowjak.hivemind.engine.systems.InputEventProcessingSystem;

import com.badlogic.gdx.ai.msg.Telegram;

/**
 * A possible state for the {@link InputEventProcessingSystem}. In this state,
 * the user has selected one or more entities. Possible transitions include:
 * <ul>
 * <li></li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
public class ActiveSelectionState implements InputSystemState {
	
	@Override
	public void enter(InputEventProcessingSystem entity) {
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update(InputEventProcessingSystem entity) {
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void exit(InputEventProcessingSystem entity) {
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onMessage(InputEventProcessingSystem entity, Telegram telegram) {
		
		// TODO Auto-generated method stub
		return false;
	}
	
}
