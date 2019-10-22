/**
 * 
 */
package org.snowjak.hivemind.gamescreen;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.collections.api.list.primitive.MutableBooleanList;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.snowjak.hivemind.App;
import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.display.Fonts;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.game.ExitGameEvent;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.ui.MouseHoverListener;
import org.snowjak.hivemind.ui.MouseHoverListener.MouseHoverListenerRegistrar;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.utils.Disposable;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidInput.KeyHandler;
import squidpony.squidgrid.gui.gdx.SquidMouse;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidmath.OrderedSet;

/**
 * Encapsulates logic surrounding the game-map display. Doesn't act as an
 * {@link Actor} in the scene-graph itself, but provides a configured Actor and
 * {@link SquidInput} upon request.
 * <p>
 * Note that this class is implemented as a <strong>singleton</strong>. This is
 * necessitated because the various {@link Engine} modules --
 * {@link EntitySystem}s, mostly -- require a reference to the active
 * {@link GameScreen} to post {@link GameScreenUpdate}s against. This
 * <em>probably</em> won't be a problem, because the {@link Engine} itself is a
 * singleton -- we won't ever have more than one active world at a time.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class GameScreen implements Disposable, MouseHoverListenerRegistrar {
	
	public static final String PREFERENCE_CELL_WIDTH = "game-screen.cell-width",
			PREFERENCE_CELL_HEIGHT = "game-screen.cell-height", PREFERENCE_MOUSE_SCROLL = "game-screen.mouse-scroll";
	{
		Config.get().register(PREFERENCE_CELL_WIDTH, "Width (in pixels) of each game-map cell", 16f, true, true);
		Config.get().register(PREFERENCE_CELL_HEIGHT, "Height (in pixels) of each game-map cell", 16f, true, true);
		Config.get().register(PREFERENCE_MOUSE_SCROLL, "Use mouse to scroll game-map screen?", true, true, false);
	}
	
	private static GameScreen __INSTANCE = null;
	
	public static GameScreen get() {
		
		if (__INSTANCE == null)
			synchronized (GameScreen.class) {
				if (__INSTANCE == null)
					__INSTANCE = new GameScreen();
			}
		
		return __INSTANCE;
	}
	
	private Container<SparseLayers> rootActor;
	private SparseLayers sparseLayers;
	private SquidInput squidInput;
	
	private OrderedSet<MouseHoverListener> hoverListeners = new OrderedSet<>();
	private MutableBooleanList activeHoverListeners = new BooleanArrayList();
	
	private BlockingQueue<GameScreenUpdate> queuedUpdates = new LinkedBlockingQueue<>();
	
	private int mouseX, mouseY;
	/**
	 * Defines the bottom-left corner of the screen's current "scroll-window"
	 */
	private float cameraX = 0,
			/**
			 * Defines the bottom-left corner of the screen's current "scroll-window"
			 */
			cameraY = 0;
	
	private MouseHoverListener scrollLeftListener, scrollRightListener, scrollUpListener, scrollDownListener;
	
	private GameScreen() {
		
		super();
		
		rootActor = new Container<>();
		// rootActor.setFillParent(true);
		// rootActor.setX(0);
		// rootActor.setY(0);
		
		this.cameraX = 0;
		this.cameraY = 0;
		
		setupScrollHoverListeners();
		
		EventBus.get().register(this);
	}
	
	/**
	 * Attempt to load the game save-file.
	 */
	public void load() {
		
	}
	
	/**
	 * Attempt load the prefab identified by the given ID.
	 * 
	 * @param id
	 */
	public void loadPrefab(int id) {
		
	}
	
	/**
	 * Once per frame, update this game's screen.
	 * 
	 * @param delta
	 */
	public void update(float delta) {
		
		//
		// Execute any outstanding updates.
		//
		while (!queuedUpdates.isEmpty()) {
			final GameScreenUpdate queuedUpdate = queuedUpdates.poll();
			if (queuedUpdate != null) {
				queuedUpdate.execute(this);
				GameScreenUpdatePool.get().retire(queuedUpdate);
			}
		}
		
		if (sparseLayers == null)
			return;
		
		if (sparseLayers.getStage() != null) {
			sparseLayers.getStage().getCamera().position.x = cameraX;
			sparseLayers.getStage().getCamera().position.y = cameraY;
		}
		
		updateHoverListeners(delta);
	}
	
	/**
	 * @return the "drawing surface" -- i.e., the {@link SparseLayers} instance
	 */
	public SparseLayers getSurface() {
		
		return sparseLayers;
	}
	
	/**
	 * Clears the {@link SparseLayers} "drawing-surface". If its current size does
	 * not match the given size, removes and re-creates the SparseLayers instance to
	 * match.
	 * <p>
	 * This method will center the GameScreen's camera at the middle of the map.
	 * </p>
	 * 
	 * @param width
	 * @param height
	 */
	public void resizeSurface(int width, int height) {
		
		if (sparseLayers != null && width == sparseLayers.getGridWidth() && height == sparseLayers.getGridHeight())
			sparseLayers.clear();
		else {
			if (sparseLayers != null)
				sparseLayers.remove();
			
			final TextCellFactory font = new TextCellFactory().font(Fonts.get().get(Fonts.FONT_MAP));
			sparseLayers = new SparseLayers(width, height, getCellWidth(), getCellHeight(), font);
			sparseLayers.fillBackground(SColor.AURORA_GRAPHITE);
			
			rootActor.setWidth(sparseLayers.getWidth());
			rootActor.setHeight(sparseLayers.getHeight());
			rootActor.setActor(sparseLayers);
			
			getSquidInput().getMouse().reinitialize(getCellWidth(), getCellHeight(), getGridWidth(), getGridHeight(), 0,
					0);
		}
		
		cameraX = (width * getCellWidth() - getWindowWidth()) / 2f;
		cameraY = (width * getCellWidth() + getWindowWidth()) / 2f;
	}
	
	/**
	 * @return the screen's {@link Actor} configured and ready for inclusion in the
	 *         scene-graph
	 */
	public Actor getActor() {
		
		return rootActor;
	}
	
	/**
	 * @return the special GameScreen-optimized {@link SquidInput}
	 */
	public SquidInput getSquidInput() {
		
		if (squidInput == null)
			squidInput = new SquidInput(new KeyHandler() {
				
				@Override
				public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
					
					switch (key) {
					case SquidInput.ESCAPE: {
						EventBus.get().post(ExitGameEvent.class);
						break;
					}
					case SquidInput.LEFT_ARROW: {
						doScroll(Direction.LEFT, (shift ? 16 : 4) * getCellWidth());
						break;
					}
					case SquidInput.RIGHT_ARROW: {
						doScroll(Direction.RIGHT, (shift ? 16 : 4) * getCellWidth());
						break;
					}
					case SquidInput.UP_ARROW: {
						doScroll(Direction.UP, (shift ? 16 : 4) * getCellHeight());
						break;
					}
					case SquidInput.DOWN_ARROW: {
						doScroll(Direction.DOWN, (shift ? 16 : 4) * getCellHeight());
						break;
					}
					}
				}
			}, new SquidMouse(getCellWidth(), getCellHeight(), getGridWidth(), getGridHeight(), 0, 0,
					new InputAdapter() {
						
						@Override
						public boolean mouseMoved(int screenX, int screenY) {
							
							mouseX = screenX;
							mouseY = screenY;
							return false;
						}
						
						@Override
						public boolean touchUp(int screenX, int screenY, int pointer, int button) {
							
							return false;
						}
						
						@Override
						public boolean touchDragged(int screenX, int screenY, int pointer) {
							
							return false;
						}
					}));
		
		return squidInput;
	}
	
	/**
	 * Attempt to scroll this GameScreen in the given {@link Direction}. If the
	 * screen is already scrolled as far as possible, this does nothing.
	 * 
	 * @param direction
	 */
	public void doScroll(Direction direction) {
		
		doScroll(direction, 1f);
	}
	
	/**
	 * Attempt to scroll this GameScreen in the given {@link Direction}. If the
	 * screen is already scrolled as far as possible, this does nothing.
	 * 
	 * @param direction
	 * @param scale
	 */
	public void doScroll(Direction direction, float scale) {
		
		final float newX = cameraX + ((float) direction.deltaX) * scale;
		final float newY = cameraY - ((float) direction.deltaY) * scale;
		
		final float worldWidth = getGridWidth() * getCellWidth(), worldHeight = getGridHeight() * getCellHeight();
		
		final float minX = 0 + (getWindowWidth() / 2f), minY = 0 + (getWindowHeight() / 2f);
		final float maxX = worldWidth - getWindowWidth() / 2f;
		final float maxY = worldHeight - getWindowHeight() / 2f;
		
		cameraX = Math.min(Math.max(newX, minX), maxX);
		cameraY = Math.min(Math.max(newY, minY), maxY);
	}
	
	/**
	 * Register a new duration-less {@link MouseHoverListener}.
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param receiver
	 * @return the configured {@link MouseHoverListener}
	 */
	@Override
	public MouseHoverListener registerHoverListener(int startX, int startY, int endX, int endY,
			MouseHoverListener.MouseHoverReceiver receiver) {
		
		final MouseHoverListener listener = new MouseHoverListener(startX, startY, endX, endY, receiver);
		hoverListeners.add(listener);
		activeHoverListeners.add(false);
		return listener;
	}
	
	/**
	 * Register a new {@link MouseHoverListener} that will fire only after the mouse
	 * has been in the given zone for {@code duration} seconds.
	 * 
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param duration
	 * @param receiver
	 * @return
	 */
	@Override
	public MouseHoverListener registerHoverListener(int startX, int startY, int endX, int endY, float duration,
			MouseHoverListener.MouseHoverReceiver receiver) {
		
		final MouseHoverListener listener = new MouseHoverListener(startX, startY, endX, endY, duration, receiver);
		hoverListeners.add(listener);
		activeHoverListeners.add(false);
		return listener;
	}
	
	/**
	 * Un-register the given {@link MouseHoverListener}.
	 * 
	 * @param listener
	 */
	@Override
	public void unregisterHoverListener(MouseHoverListener listener) {
		
		final int index = hoverListeners.indexOf(listener);
		if (index < 0)
			return;
		
		hoverListeners.removeAt(index);
		activeHoverListeners.removeAtIndex(index);
	}
	
	protected void updateHoverListeners(float delta) {
		
		for (int i = 0; i < hoverListeners.size(); i++) {
			final MouseHoverListener hover = hoverListeners.getAt(i);
			
			final boolean isActive = hover.isActive(mouseX, mouseY);
			final boolean prevActive = activeHoverListeners.get(i);
			if (prevActive ^ isActive) {
				hover.reset();
				activeHoverListeners.set(i, isActive);
			}
			
			if (isActive)
				if (hover.isSatisfied(mouseX, mouseY, delta))
					hover.call(mouseX, mouseY);
		}
	}
	
	/**
	 * Un-register any previously-registered and, if enabled in configuration,
	 * re-creates the scrolling {@link MouseHoverListener}s.
	 */
	private void setupScrollHoverListeners() {
		
		final int onscreenGridWidth = (int) (getWindowWidth() / getCellWidth());
		final int onscreenGridHeight = (int) (getWindowHeight() / getCellHeight());
		
		if (scrollLeftListener != null)
			unregisterHoverListener(scrollLeftListener);
		if (scrollRightListener != null)
			unregisterHoverListener(scrollRightListener);
		if (scrollUpListener != null)
			unregisterHoverListener(scrollUpListener);
		if (scrollDownListener != null)
			unregisterHoverListener(scrollDownListener);
		
		if (Config.get().getBoolean(PREFERENCE_MOUSE_SCROLL)) {
			
			final int bufferWidth = onscreenGridWidth / 8;
			final int bufferHeight = onscreenGridHeight / 8;
			
			scrollLeftListener = this.registerHoverListener(0, 0, bufferWidth, onscreenGridHeight,
					(x, y) -> doScroll(Direction.LEFT, bufferWidth - x));
			scrollRightListener = this.registerHoverListener(onscreenGridWidth - bufferWidth, 0, onscreenGridWidth,
					onscreenGridHeight, (x, y) -> doScroll(Direction.RIGHT, x - (onscreenGridWidth - bufferWidth) + 1));
			scrollUpListener = this.registerHoverListener(0, 0, onscreenGridWidth, bufferHeight,
					(x, y) -> doScroll(Direction.UP, bufferHeight - y));
			scrollDownListener = this.registerHoverListener(0, onscreenGridHeight - bufferHeight, onscreenGridWidth,
					onscreenGridHeight,
					(x, y) -> doScroll(Direction.DOWN, y - (onscreenGridHeight - bufferHeight) + 1));
		}
	}
	
	/**
	 * Add the given {@link GameScreenUpdate} to this GameScreen's internal queue of
	 * updates.
	 * <p>
	 * This method is thread-safe, and can safely be called by multiple threads
	 * simultaneously.
	 * </p>
	 * 
	 * @param update
	 */
	public void postGameScreenUpdate(GameScreenUpdate update) {
		
		queuedUpdates.offer(update);
	}
	
	public float getWindowWidth() {
		
		return Config.get().getInt(App.PREFERENCE_WINDOW_WIDTH);
	}
	
	public float getWindowHeight() {
		
		return Config.get().getInt(App.PREFERENCE_WINDOW_HEIGHT);
	}
	
	public float getCellWidth() {
		
		return Config.get().getFloat(PREFERENCE_CELL_WIDTH);
	}
	
	public float getCellHeight() {
		
		return Config.get().getFloat(PREFERENCE_CELL_HEIGHT);
	}
	
	public int getGridWidth() {
		
		if (sparseLayers != null)
			return sparseLayers.getGridWidth();
		
		return 0;
	}
	
	public int getGridHeight() {
		
		if (sparseLayers != null)
			return sparseLayers.getGridHeight();
		
		return 0;
	}
	
	@Override
	public void dispose() {
		
		EventBus.get().unregister(this);
	}
}
