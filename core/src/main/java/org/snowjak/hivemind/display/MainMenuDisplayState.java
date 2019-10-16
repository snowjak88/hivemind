/**
 * 
 */
package org.snowjak.hivemind.display;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;

/**
 * In this {@link State}, the {@link Display} presents the main menu.
 * 
 * @author snowjak88
 *
 */
public class MainMenuDisplayState implements DisplayState {
	
	@Override
	public void enter(Display entity) {
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void update(Display entity) {
		
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void exit(Display entity) {
		
		entity.setRoot(null);
	}
	
	@Override
	public boolean onMessage(Display entity, Telegram telegram) {
		
		// TODO Auto-generated method stub
		return false;
	}
	
}
