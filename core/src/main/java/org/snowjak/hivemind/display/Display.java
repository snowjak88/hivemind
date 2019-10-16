/**
 * 
 */
package org.snowjak.hivemind.display;

import org.snowjak.hivemind.App;
import org.snowjak.hivemind.config.Config;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.fsm.DefaultStateMachine;
import com.badlogic.gdx.ai.fsm.StateMachine;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import squidpony.squidgrid.gui.gdx.DefaultResources;
import squidpony.squidgrid.gui.gdx.FilterBatch;
import squidpony.squidgrid.gui.gdx.FloatFilters;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.TextCellFactory;

/**
 * A Display presents an interface to the game-window. UI elements, including
 * the main screen, are presented within the context of a Display.
 * 
 * @author snowjak88
 *
 */
public class Display implements Disposable {
	
	public static final TextCellFactory DEFAULT_FONT = DefaultResources.getStretchableSquareFont();
	
	private StateMachine<Display, DisplayState> displayStateMachine;
	
	private FilterBatch batch;
	private Viewport mainViewport;
	private Stage stage;
	
	private Actor rootActor = null;
	
	private Color background = SColor.BLACK;
	
	public void created() {
		
		displayStateMachine = new DefaultStateMachine<>(this, new MainMenuDisplayState());
		
		batch = new FilterBatch(FloatFilters.identityFilter);
		mainViewport = new StretchViewport(Config.get().getInt(App.PREFERENCE_WINDOW_WIDTH),
				Config.get().getInt(App.PREFERENCE_WINDOW_HEIGHT));
		stage = new Stage(mainViewport, batch);
		
		Gdx.input.setInputProcessor(new InputMultiplexer(stage));
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
	
	public void render(float delta) {
		
		displayStateMachine.update();
		
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
	
	public void resize(int width, int height) {
		
		stage.getViewport().update(width, height, false);
		stage.getViewport().setScreenBounds(0, 0, width, height);
	}
	
	public void dispose() {
		
		stage.dispose();
		batch.dispose();
	}
}
