/**
 * 
 */
package org.snowjak.hivemind.display;

import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.concurrent.PerFrameProcess;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.EngineUpdatePerFrameProcess;
import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.game.ExitGameEvent;
import org.snowjak.hivemind.gamescreen.GameScreen;
import org.snowjak.hivemind.gamescreen.updates.ClearMapUpdate;
import org.snowjak.hivemind.gamescreen.updates.RemoveAllGlyphsUpdate;

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
	
	private static final Logger LOG = Logger.getLogger(GameScreenDisplayState.class.getName());
	
	private final PerFrameProcess engineUpdateProcess = new EngineUpdatePerFrameProcess();
	private boolean exitGame = false;
	
	@Override
	public void enter(Display entity) {
		
		final GameScreen gameScreen = new GameScreen();
		Context.setGameScreen(gameScreen);
		
		gameScreen.postGameScreenUpdate(new ClearMapUpdate());
		gameScreen.postGameScreenUpdate(new RemoveAllGlyphsUpdate());
		
		entity.setRoot(gameScreen.getActor());
		entity.setInput(gameScreen.getInputProcessor());
		
		engineUpdateProcess.setOnProcessCrash((t) -> {
			LOG.severe("The game-world subsystem has crashed due to an unhandled exception: "
					+ t.getClass().getSimpleName() + ": '" + t.getMessage() + "'");
			exitGame = true;
		});
		engineUpdateProcess.start();
		
		exitGame = false;
		
		EventBus.get().register(this);
	}
	
	@Override
	public void update(Display entity) {
		
		Context.getGameScreen().update(entity.getDelta());
		
		if (exitGame)
			entity.getDisplayStateMachine().changeState(new SaveEngineStateDisplayState());
	}
	
	@Override
	public void exit(Display entity) {
		
		entity.setRoot(null);
		entity.setInput(null);
		
		entity.getStage().getCamera().position.x = entity.getStage().getViewport().getWorldWidth() / 2;
		entity.getStage().getCamera().position.y = entity.getStage().getViewport().getWorldHeight() / 2;
		
		engineUpdateProcess.kill();
		
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
