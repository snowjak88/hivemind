/**
 * 
 */
package org.snowjak.hivemind.display;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.EnginePrefabs;

import com.badlogic.gdx.ai.msg.Telegram;

/**
 * A temporary {@link DisplayState} in which we load the appropriate
 * {@link Engine} prefab.
 * 
 * @author snowjak88
 *
 */
public class LoadEnginePrefabDisplayState implements DisplayState {
	
	@Override
	public void enter(Display entity) {
		
		// Nothing to do
	}
	
	@Override
	public void update(Display entity) {
		
		Context.setEngine(new Engine());
		EnginePrefabs.loadTest();
		
		entity.getDisplayStateMachine().changeState(new GameScreenDisplayState());
	}
	
	@Override
	public void exit(Display entity) {
		
		// Nothing to do
	}
	
	@Override
	public boolean onMessage(Display entity, Telegram telegram) {
		
		return false;
	}
	
}
