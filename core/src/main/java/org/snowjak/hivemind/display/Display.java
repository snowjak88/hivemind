/**
 * 
 */
package org.snowjak.hivemind.display;

import org.snowjak.hivemind.App;
import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.gamescreen.UpdateableInputProcessor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import squidpony.squidgrid.gui.gdx.FilterBatch;
import squidpony.squidgrid.gui.gdx.FloatFilters;
import squidpony.squidgrid.gui.gdx.SColor;

/**
 * A Display presents an interface to the game-window. UI elements, including
 * the main screen, are presented within the context of a Display.
 * 
 * @author snowjak88
 *
 */
public class Display implements Disposable {
	
	private StateMachine<Display, DisplayState> displayStateMachine;
	
	private FilterBatch batch;
	private Viewport mainViewport;
	private Stage stage;
	private InputMultiplexer inputMultiplexer;
	
	private float delta = 0;
	
	private Actor rootActor = null;
	private UpdateableInputProcessor inputProcessor = null;
	
	private Color background = SColor.BLACK;
	
	public void created() {
		
		batch = new FilterBatch(FloatFilters.identityFilter);
		mainViewport = new StretchViewport(Config.get().getInt(App.PREFERENCE_WINDOW_WIDTH),
				Config.get().getInt(App.PREFERENCE_WINDOW_HEIGHT));
		stage = new Stage(mainViewport, batch);
		
		displayStateMachine = new DefaultStateMachine<>(this);
		displayStateMachine.changeState(new MainMenuDisplayState());
		
		inputMultiplexer = new InputMultiplexer(stage);
		Gdx.input.setInputProcessor(inputMultiplexer);
	}
	
	/**
	 * Set the {@link Actor} at the root of this Display's scene-graph.
	 * 
	 * @param rootActor
	 */
	public void setRoot(Actor rootActor) {
		
		if (this.rootActor != null) {
			final Actor oldRoot = this.rootActor;
			this.rootActor = null;
			oldRoot.remove();
		}
		
		this.rootActor = rootActor;
		
		if (this.rootActor != null)
			stage.addActor(this.rootActor);
	}
	
	/**
	 * If necessary, you can provide a special {@link InputProcessor} instance which
	 * will receive all input-events <em>before</em> the normal LibGDX-UI
	 * input-handling system.
	 * 
	 * @param inputProcessor
	 *            {@code null} to remove any special InputProcessor assignment and
	 *            revert all input-handling back to LibGDX-UI's default
	 *            input-handling system
	 */
	public void setInput(UpdateableInputProcessor inputProcessor) {
		
		if (inputProcessor == null)
			inputMultiplexer.setProcessors(stage);
		else
			inputMultiplexer.setProcessors(inputProcessor, stage);
		
		this.inputProcessor = inputProcessor;
	}
	
	public void render(float delta) {
		
		this.delta = delta;
		displayStateMachine.update();
		
		if (inputProcessor != null)
			inputProcessor.update(delta);
		
		Gdx.gl.glClearColor(background.r, background.g, background.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		stage.act();
		stage.getViewport().apply(false);
		
		batch.setProjectionMatrix(stage.getCamera().combined);
		
		batch.begin();
		
		stage.getRoot().draw(batch, 1);
		
		batch.end();
		
		Gdx.graphics.setTitle("FPS: " + Gdx.graphics.getFramesPerSecond());
	}
	
	/**
	 * @return the {@link StateMachine} managing this Display's different states
	 */
	public StateMachine<Display, DisplayState> getDisplayStateMachine() {
		
		return displayStateMachine;
	}
	
	/**
	 * @return the last-received "delta" -- i.e., "seconds since last frame"
	 */
	public float getDelta() {
		
		return delta;
	}
	
	public Stage getStage() {
		
		return stage;
	}
	
	public void resize(int width, int height) {
		
		stage.getViewport().update(width, height, false);
		stage.getViewport().setScreenBounds(0, 0, width, height);
	}
	
	public void dispose() {
		
		stage.dispose();
		batch.dispose();
	}
}
