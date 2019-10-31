/**
 * 
 */
package org.snowjak.hivemind.gamescreen;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.collections.api.iterator.MutableIntIterator;
import org.eclipse.collections.api.map.primitive.MutableObjectIntMap;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectIntHashMap;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;
import org.snowjak.hivemind.App;
import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.display.Fonts;
import org.snowjak.hivemind.engine.systems.ToyEntityRemovingSystem;
import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.game.ExitGameEvent;
import org.snowjak.hivemind.events.input.GameKey;
import org.snowjak.hivemind.events.input.InputEvent;
import org.snowjak.hivemind.events.input.InputEvent.MouseButton;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.ui.MouseHoverListener;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.utils.Disposable;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.SparseTextMap;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Encapsulates logic surrounding the game-map display. Doesn't act as an
 * {@link Actor} in the scene-graph itself, but provides a configured Actor and
 * {@link SquidInput} upon request.
 * <h3>Input-Handling</h3>
 * <p>
 * The GameScreen will set up {@link InputEventListener}s for the following
 * events:
 * <ul>
 * <li>Mouse-hover to scroll the map</li>
 * <li>Arrow-keys to scroll the map</li>
 * <li>{@code ESCAPE} to close the GameScreen</li>
 * </ul>
 * </p>
 * <p>
 * Additional {@link InputEvent}s may be listened-for by calling
 * {@link #getInputProcessor()}.
 * {@link GameScreenInputProcessor#registerInputListener(InputEventListener)
 * registerInputListener(InputEventListener)}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class GameScreen implements Disposable, ScreenMapTranslator {
	
	public static final String PREFERENCE_CELL_WIDTH = "game-screen.cell-width",
			PREFERENCE_CELL_HEIGHT = "game-screen.cell-height", PREFERENCE_MOUSE_SCROLL = "game-screen.mouse-scroll";
	{
		Config.get().register(PREFERENCE_CELL_WIDTH, "Width (in pixels) of each game-map cell", 16f, true, true);
		Config.get().register(PREFERENCE_CELL_HEIGHT, "Height (in pixels) of each game-map cell", 16f, true, true);
		Config.get().register(PREFERENCE_MOUSE_SCROLL, "Use mouse to scroll game-map screen?", true, true, false);
	}
	
	public static final SColor NOT_VISIBLE_DARKNESS = SColor.INK;
	public static final float NOT_VISIBLE_DARKNESS_FLOAT = NOT_VISIBLE_DARKNESS.toFloatBits();
	
	private Container<SparseLayers> rootActor;
	private SparseLayers sparseLayers;
	private GameScreenInputProcessor inputProcessor;
	
	private float gridScreenWidth = -1, gridScreenHeight = -1;
	
	private final MutableObjectIntMap<String> layerNamesToIndices = new ObjectIntHashMap<>();
	private final MutableIntSet freeLayerIndices = new IntHashSet();
	
	private Glyph cursor = null;
	
	private BlockingQueue<GameScreenUpdate> queuedUpdates = new LinkedBlockingQueue<>();
	
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
		
		rootActor = new Container<>();
		
		this.cameraX = 0;
		this.cameraY = 0;
		
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
			
			rootActor.setWidth(sparseLayers.getWidth());
			rootActor.setHeight(sparseLayers.getHeight());
			rootActor.setActor(sparseLayers);
			
			cursor = sparseLayers.glyph('\u2588', SColor.multiplyAlpha(SColor.AURORA_CLOUD, 0.25f), 0, 0);
			
			getInputProcessor().resize(getCellWidth(), getCellHeight(), getGridWorldWidth(), getGridWorldHeight(), 0,
					0);
		}
		
		cameraX = (width * getCellWidth()) / 2f;
		cameraY = (width * getCellWidth()) / 2f;
	}
	
	/**
	 * @return the screen's {@link Actor} configured and ready for inclusion in the
	 *         scene-graph
	 */
	public Actor getActor() {
		
		return rootActor;
	}
	
	/**
	 * @return the special {@link GameScreenInputProcessor}
	 */
	public GameScreenInputProcessor getInputProcessor() {
		
		if (inputProcessor == null) {
			inputProcessor = new GameScreenInputProcessor(0, 0, getCellWidth(), getCellHeight(), getGridWorldWidth(),
					getGridWorldHeight(), this);
			
			setupScrollHoverListeners();
			
			inputProcessor.registerInputListener(InputEventListener.build().continuous().onEvent((e) -> {
				if (cursor == null || sparseLayers == null)
					return;
				
				final float x = sparseLayers.worldX(e.getMapCursor().x);
				final float y = sparseLayers.worldY(e.getMapCursor().y);
				cursor.setPosition(x, y);
			}).get());
			
			inputProcessor
					.registerInputListener(
							InputEventListener.build().button(MouseButton.LEFT_BUTTON)
									.one(GameKey.ALT_LEFT, GameKey.ALT_RIGHT).onEvent((e) -> Context.getEngine()
											.getSystem(ToyEntityRemovingSystem.class).postRequest(e.getMapCursor()))
									.get());
			
			inputProcessor.registerInputListener(
					InputEventListener.build().all(GameKey.UP).onEvent((e) -> doScroll(Direction.UP, 16f)).get());
			inputProcessor.registerInputListener(
					InputEventListener.build().all(GameKey.DOWN).onEvent((e) -> doScroll(Direction.DOWN, 16f)).get());
			inputProcessor.registerInputListener(
					InputEventListener.build().all(GameKey.LEFT).onEvent((e) -> doScroll(Direction.LEFT, 16f)).get());
			inputProcessor.registerInputListener(
					InputEventListener.build().all(GameKey.RIGHT).onEvent((e) -> doScroll(Direction.RIGHT, 16f)).get());
			
			inputProcessor.registerInputListener(InputEventListener.build().all(GameKey.ESCAPE)
					.onEvent((e) -> EventBus.get().post(ExitGameEvent.class)).get());
		}
		
		return inputProcessor;
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
		
		final float worldWidth = getGridWorldWidth() * getCellWidth(),
				worldHeight = getGridWorldHeight() * getCellHeight();
		
		final float minX = 0 + (getWindowWidth() / 2f), minY = 0 + (getWindowHeight() / 2f);
		final float maxX = worldWidth - getWindowWidth() / 2f;
		final float maxY = worldHeight - getWindowHeight() / 2f;
		
		cameraX = Math.min(Math.max(newX, minX), maxX);
		cameraY = Math.min(Math.max(newY, minY), maxY);
	}
	
	/**
	 * Un-register any previously-registered and, if enabled in configuration,
	 * re-creates the scrolling {@link MouseHoverListener}s.
	 */
	private void setupScrollHoverListeners() {
		
		final int onscreenGridWidth = (int) (getWindowWidth() / getCellWidth());
		final int onscreenGridHeight = (int) (getWindowHeight() / getCellHeight());
		
		if (scrollLeftListener != null)
			getInputProcessor().unregisterHoverListener(scrollLeftListener);
		if (scrollRightListener != null)
			getInputProcessor().unregisterHoverListener(scrollRightListener);
		if (scrollUpListener != null)
			getInputProcessor().unregisterHoverListener(scrollUpListener);
		if (scrollDownListener != null)
			getInputProcessor().unregisterHoverListener(scrollDownListener);
		
		if (Config.get().getBoolean(PREFERENCE_MOUSE_SCROLL)) {
			
			final int bufferWidth = onscreenGridWidth / 8;
			final int bufferHeight = onscreenGridHeight / 8;
			
			scrollLeftListener = getInputProcessor().registerHoverListener(0, 0, bufferWidth, onscreenGridHeight,
					(x, y) -> doScroll(Direction.LEFT, bufferWidth - x));
			scrollRightListener = getInputProcessor().registerHoverListener(onscreenGridWidth - bufferWidth, 0,
					onscreenGridWidth, onscreenGridHeight,
					(x, y) -> doScroll(Direction.RIGHT, x - (onscreenGridWidth - bufferWidth) + 1));
			scrollUpListener = getInputProcessor().registerHoverListener(0, 0, onscreenGridWidth, bufferHeight,
					(x, y) -> doScroll(Direction.UP, bufferHeight - y));
			scrollDownListener = getInputProcessor().registerHoverListener(0, onscreenGridHeight - bufferHeight,
					onscreenGridWidth, onscreenGridHeight,
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
	
	/**
	 * Get the {@link SparseTextMap layer} associated with the given {@code name},
	 * or {@code null} if {@code name == null}.
	 * 
	 * @param name
	 * @return
	 */
	public SparseTextMap getNamedLayer(String name) {
		
		if (name == null)
			return null;
		
		return sparseLayers.addLayer(getNamedLayerIndex(name));
	}
	
	protected int getNamedLayerIndex(String name) {
		
		if (layerNamesToIndices.containsKey(name))
			return layerNamesToIndices.get(name);
		
		final int freeIndex;
		if (!freeLayerIndices.isEmpty()) {
			
			final MutableIntIterator iterator = freeLayerIndices.intIterator();
			freeIndex = iterator.next();
			iterator.remove();
			
		} else
			freeIndex = sparseLayers.getLayerCount();
		
		layerNamesToIndices.put(name, freeIndex);
		return freeIndex;
	}
	
	/**
	 * If the given {@code name} is associated with a {@link SparseTextMap layer},
	 * removes that association. Also clears the associated layer.
	 * 
	 * @param name
	 */
	public void unassociateNamedLayer(String name) {
		
		if (name == null)
			return;
		
		if (!layerNamesToIndices.containsKey(name))
			return;
		
		final int index = layerNamesToIndices.get(name);
		final SparseTextMap layer = sparseLayers.getLayer(index);
		if (layer != null)
			layer.clear();
		
		layerNamesToIndices.remove(name);
		freeLayerIndices.add(index);
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
	
	public float getGridScreenWidth() {
		
		if (gridScreenWidth < 0)
			gridScreenWidth = getWindowWidth() / getCellWidth();
		return gridScreenWidth;
	}
	
	public float getGridScreenHeight() {
		
		if (gridScreenHeight < 0)
			gridScreenHeight = getWindowHeight() / getCellHeight();
		return gridScreenHeight;
	}
	
	public int getGridWorldWidth() {
		
		if (sparseLayers != null)
			return sparseLayers.getGridWidth();
		
		return 0;
	}
	
	public int getGridWorldHeight() {
		
		if (sparseLayers != null)
			return sparseLayers.getGridHeight();
		
		return 0;
	}
	
	@Override
	public int screenToMapX(int screenX) {
		
		if (sparseLayers == null)
			return 0;
		
		final int leftGridCell = sparseLayers.gridX(cameraX - getWindowWidth() / 2f);
		return screenX + leftGridCell;
	}
	
	@Override
	public int screenToMapY(int screenY) {
		
		if (sparseLayers == null)
			return 0;
		
		final int topGridCell = sparseLayers.gridY(cameraY + getWindowHeight() / 2f);
		return screenY + topGridCell;
	}
	
	@Override
	public int mapToScreenX(int mapX) {
		
		if (sparseLayers == null)
			return 0;
		
		final int leftGridCell = sparseLayers.gridX(cameraX - getWindowWidth() / 2f);
		return mapX - leftGridCell;
	}
	
	@Override
	public int mapToScreenY(int mapY) {
		
		if (sparseLayers == null)
			return 0;
		
		final int topGridCell = sparseLayers.gridY(cameraY + getWindowHeight() / 2f);
		return mapY - topGridCell;
	}
	
	@Override
	public void dispose() {
		
		EventBus.get().unregister(this);
	}
}
