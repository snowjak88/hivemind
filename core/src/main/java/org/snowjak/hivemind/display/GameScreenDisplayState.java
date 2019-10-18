/**
 * 
 */
package org.snowjak.hivemind.display;

import org.snowjak.hivemind.concurrent.PerFrameProcess;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.EngineUpdatePerFrameProcess;
import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.game.ExitGameEvent;
import org.snowjak.hivemind.ui.gamescreen.GameScreen;

import com.badlogic.gdx.ai.msg.Telegram;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

/**
 * In this {@link DisplayState}, the {@link Display} displays the
 * {@link GameScreen}.
 * <p>
 * Note that this state expects the {@link Engine} to have been configured
 * already -- e.g., by the {@link LoadEnginePrefabDisplayState}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class GameScreenDisplayState implements DisplayState {
	
	private final PerFrameProcess engineUpdateProcess = new EngineUpdatePerFrameProcess();
	private final GameScreen gameScreen = GameScreen.get();
	private boolean exitGame = false;
	
	@Override
	public void enter(Display entity) {
		
		entity.setRoot(gameScreen.getActor());
		entity.setInput(gameScreen.getSquidInput());
		
		engineUpdateProcess.start();
		
		exitGame = false;
		
		EventBus.get().register(this);
	}
	
	@Override
	public void update(Display entity) {
		
		final float delta = entity.getDelta();
		engineUpdateProcess.update(delta);
		gameScreen.update(delta);
		
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
