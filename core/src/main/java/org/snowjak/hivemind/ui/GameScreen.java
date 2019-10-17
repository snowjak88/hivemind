/**
 * 
 */
package org.snowjak.hivemind.ui;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.collections.api.list.primitive.MutableBooleanList;
import org.eclipse.collections.impl.list.mutable.primitive.BooleanArrayList;
import org.snowjak.hivemind.App;
import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.display.Fonts;
import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.game.ExitGameEvent;
import org.snowjak.hivemind.ui.MouseHoverListener.MouseHoverListenerRegistrar;

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
 * {@link Actor} in the scene-graph itself, but provides a configured Actor upon
 * request.
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
	
	private Container<SparseLayers> rootActor;
	private SparseLayers sparseLayers;
	private SquidInput squidInput;
	
	private OrderedSet<MouseHoverListener> hoverListeners = new OrderedSet<>();
	private MutableBooleanList activeHoverListeners = new BooleanArrayList();
	
	private BlockingQueue<Runnable> queuedRunnables = new LinkedBlockingQueue<>();
	
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
	
	public GameScreen() {
		
		super();
		
		final TextCellFactory font = new TextCellFactory().font(Fonts.get().get(Fonts.FONT_MAP));
		
		this.rootActor = new Container<SparseLayers>();
		this.rootActor.setFillParent(true);
		this.rootActor.setClip(true);
		
		this.sparseLayers = new SparseLayers(getGridWidth(), getGridHeight(), getCellWidth(), getCellHeight(), font);
		this.sparseLayers.put(getGridWidth() / 2, getGridHeight() / 2, 'X', SColor.RED);
		this.sparseLayers.put(getGridWidth() / 2, getGridHeight() / 2 - 1, "That should be in the center",
				SColor.WHITE);
		
		for (int x = 0; x < getGridWidth(); x++) {
			this.sparseLayers.put(x, 0, '#', SColor.WHITE);
			this.sparseLayers.put(x, getGridHeight() - 1, '#', SColor.WHITE);
		}
		for (int y = 0; y < getGridHeight(); y++) {
			this.sparseLayers.put(0, y, '#', SColor.WHITE);
			this.sparseLayers.put(getGridWidth() - 1, y, '#', SColor.WHITE);
		}
		
		this.rootActor.setActor(sparseLayers);
		
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
		
		while (!queuedRunnables.isEmpty()) {
			final Runnable queuedRunnable = queuedRunnables.poll();
			if (queuedRunnable != null)
				queuedRunnable.run();
		}
		
		sparseLayers.setX(-cameraX);
		sparseLayers.setY(-cameraY);
		
		updateHoverListeners(delta);
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
		
		final float minX = 0, minY = 0;
		final float maxX = (getGridWidth() * getCellWidth()) - getWindowWidth();
		final float maxY = (getGridHeight() * getCellHeight()) - getWindowHeight();
		
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
					onscreenGridHeight, (x, y) -> doScroll(Direction.RIGHT, x - (onscreenGridWidth - bufferWidth)));
			scrollUpListener = this.registerHoverListener(0, 0, onscreenGridWidth, bufferHeight,
					(x, y) -> doScroll(Direction.UP, bufferHeight - y));
			scrollDownListener = this.registerHoverListener(0, onscreenGridHeight - bufferHeight, onscreenGridWidth,
					onscreenGridHeight, (x, y) -> doScroll(Direction.DOWN, y - (onscreenGridHeight - bufferHeight)));
		}
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
		
		return 64;
	}
	
	public int getGridHeight() {
		
		return 64;
	}
	
	@Override
	public void dispose() {
		
		EventBus.get().unregister(this);
	}
}
