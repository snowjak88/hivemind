package org.snowjak.hivemind;

import java.time.Duration;
import java.time.Instant;

import org.snowjak.hivemind.concurrent.ContinuousProcess;
import org.snowjak.hivemind.concurrent.Executor;
import org.snowjak.hivemind.concurrent.PerFrameProcess;
import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.display.Display;
import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.EventPool;
import org.snowjak.hivemind.events.ExitAppEvent;
import org.snowjak.hivemind.util.Profiler;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

import squidpony.squidmath.CoordPacker;

public class App extends ApplicationAdapter {
	
	public static final String PREFERENCE_WINDOW_WIDTH = "window.width", PREFERENCE_WINDOW_HEIGHT = "window.height",
			PREFERENCE_WINDOW_MIN_WIDTH = "window.min-width", PREFERENCE_WINDOW_MIN_HEIGHT = "window.min-height";
	{
		Config.get().register(PREFERENCE_WINDOW_WIDTH, "Game window width (in pixels)", 800, true, true);
		Config.get().register(PREFERENCE_WINDOW_HEIGHT, "Game window height (in pixels)", 600, true, true);
		Config.get().register(PREFERENCE_WINDOW_MIN_WIDTH, "Game window minimum width (in pixels)", 800, false, true);
		Config.get().register(PREFERENCE_WINDOW_MIN_HEIGHT, "Game window minimum height (in pixels)", 600, false, true);
	}
	
	private Instant lastFrame = null;
	
	private Display display = new Display();
	
	@Override
	public void create() {
		
		CoordPacker.init();
		display.created();
		
		EventBus.get().register(this);
	}
	
	@Override
	public void render() {
		
		final Instant thisFrame = Instant.now();
		final float secondsSinceFrame;
		if (lastFrame == null)
			secondsSinceFrame = 0;
		else {
			secondsSinceFrame = ((float) Duration.between(lastFrame, thisFrame).toNanos()) / 1e-9f;
		}
		
		GdxAI.getTimepiece().update(secondsSinceFrame);
		MessageManager.getInstance().update();
		
		PerFrameProcess.getActiveProcesses().forEach(p -> p.update(secondsSinceFrame));
		
		display.render(secondsSinceFrame);
		
		lastFrame = thisFrame;
	}
	
	@Override
	public void resize(int width, int height) {
		
		super.resize(width, height);
		
		Config.get().set(PREFERENCE_WINDOW_WIDTH, width);
		Config.get().set(PREFERENCE_WINDOW_HEIGHT, height);
		
		display.resize(width, height);
	}
	
	@Override
	public void dispose() {
		
		EventBus.get().unregister(this);
		
		display.dispose();
		
		ContinuousProcess.getActiveProcesses().forEach(p -> p.stop());
		PerFrameProcess.getActiveProcesses().forEach(p -> p.stop());
		Executor.get().shutdownNow();
		Config.get().save();
		
		Profiler.get().report();
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void receiveExitGameEvent(ExitAppEvent event) {
		
		Gdx.app.postRunnable(() -> Gdx.app.exit());
		EventPool.get().retire(event);
	}
}
