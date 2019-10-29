/**
 * 
 */
package org.snowjak.hivemind.display;

import java.io.IOException;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.Engine;

import com.badlogic.gdx.ai.msg.Telegram;

/**
 * A temporary {@link DisplayState} in which we save the {@link Engine}'s state
 * to the save-file.
 * 
 * @author snowjak88
 *
 */
public class SaveEngineStateDisplayState implements DisplayState {
	
	@Override
	public void enter(Display entity) {
		
		// Nothing to do.
	}
	
	@Override
	public void update(Display entity) {
		
		try {
			
			Context.getEngine().save();
			
			Context.getGameScreen().dispose();
			Context.getEngine().clear();
			
			Context.setGameScreen(null);
			Context.setEngine(null);
			
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
		
		entity.getDisplayStateMachine().changeState(new MainMenuDisplayState());
	}
	
	@Override
	public void exit(Display entity) {
		
		// Nothing to do.
	}
	
	@Override
	public boolean onMessage(Display entity, Telegram telegram) {
		
		// TODO Auto-generated method stub
		return false;
	}
	
}
