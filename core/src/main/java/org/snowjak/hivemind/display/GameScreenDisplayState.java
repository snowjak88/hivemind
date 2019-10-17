/**
 * 
 */
package org.snowjak.hivemind.display;

import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.game.ExitGameEvent;
import org.snowjak.hivemind.ui.GameScreen;

import com.badlogic.gdx.ai.fsm.State;
import com.badlogic.gdx.ai.msg.Telegram;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * In this {@link State}, the {@link Display} displays the {@link GameScreen}.
 * 
 * @author snowjak88
 *
 */
public class GameScreenDisplayState implements DisplayState {
	
	private final GameScreen gameScreen = new GameScreen();
	private boolean exitGame = false;
	
	@Override
	public void enter(Display entity) {
		
		entity.setRoot(gameScreen.getActor());
		entity.setInput(gameScreen.getSquidInput());
		
		exitGame = false;
		
		EventBus.get().register(this);
	}
	
	@Override
	public void update(Display entity) {
		
		gameScreen.update(entity.getDelta());
		
		if (exitGame)
			entity.getDisplayStateMachine().changeState(new MainMenuDisplayState());
	}
	
	@Override
	public void exit(Display entity) {
		
		entity.setRoot(null);
		entity.setInput(null);
		
		EventBus.get().unregister(this);
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void receiveExitGameEvent(ExitGameEvent event) {
		
		exitGame = true;
	}
	
	@Override
	public boolean onMessage(Display entity, Telegram telegram) {
		
		// TODO Auto-generated method stub
		return false;
	}
	
}
