package org.snowjak.hivemind;

import java.time.Duration;
import java.time.Instant;

import org.snowjak.hivemind.concurrent.ContinuousProcess;
import org.snowjak.hivemind.concurrent.Executor;
import org.snowjak.hivemind.concurrent.PerFrameProcess;
import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.util.Profiler;

import com.badlogic.gdx.ApplicationAdapter;

import squidpony.squidmath.CoordPacker;

public class App extends ApplicationAdapter {
	
	public static final String PREFERENCE_WINDOW_WIDTH = "window.width", PREFERENCE_WINDOW_HEIGHT = "window.height",
			PREFERENCE_WINDOW_MIN_WIDTH = "window.min-width", PREFERENCE_WINDOW_MIN_HEIGHT = "window.min-height";
	
	private Instant lastFrame = null;
	
	@Override
	public void create() {
		
		CoordPacker.init();
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
		
		PerFrameProcess.getActiveProcesses().forEach(p -> p.update(secondsSinceFrame));
		
		lastFrame = thisFrame;
	}
	
	@Override
	public void resize(int width, int height) {
		
		super.resize(width, height);
		
		Config.get().set(PREFERENCE_WINDOW_WIDTH, width);
		Config.get().set(PREFERENCE_WINDOW_HEIGHT, height);
	}
	
	@Override
	public void dispose() {
		
		ContinuousProcess.getActiveProcesses().forEach(p -> p.stop());
		PerFrameProcess.getActiveProcesses().forEach(p -> p.stop());
		Executor.get().shutdownNow();
		Config.get().save();
		
		Profiler.get().report();
	}
}
