/**
 * 
 */
package org.snowjak.hivemind.display;

import java.io.IOException;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.Engine;

import com.badlogic.gdx.ai.msg.Telegram;

/**
 * A temporary {@link DisplayState} in which we load the saved {@link Engine}'s
 * state from the save-file.
 * 
 * @author snowjak88
 *
 */
public class LoadSavedEngineDisplayState implements DisplayState {
	
	@Override
	public void enter(Display entity) {
		
		// Nothing to do.
	}
	
	@Override
	public void update(Display entity) {
		
		try {
			
			Context.setEngine(new Engine());
			Context.getEngine().load();
			entity.getDisplayStateMachine().changeState(new GameScreenDisplayState());
			
		} catch (IOException e) {
			e.printStackTrace(System.err);
			entity.getDisplayStateMachine().changeState(new MainMenuDisplayState());
		}
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
