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
import org.snowjak.hivemind.config.Config;
import org.snowjak.hivemind.display.Fonts;
import org.snowjak.hivemind.events.EventBus;
import org.snowjak.hivemind.events.game.ExitGameEvent;
import org.snowjak.hivemind.events.input.GameKey;
import org.snowjak.hivemind.events.input.InputEvent;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.SparseTextMap;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.Coord;

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
			PREFERENCE_CELL_HEIGHT = "game-screen.cell-height", PREFERENCE_SIDEBAR_WIDTH = "game-screen.sidebar-width",
			PREFERENCE_MOUSE_SCROLL = "game-screen.mouse-scroll";
	{
		Config.get().register(PREFERENCE_CELL_WIDTH, "Width (in pixels) of each game-map cell", 16f, true, true);
		Config.get().register(PREFERENCE_CELL_HEIGHT, "Height (in pixels) of each game-map cell", 16f, true, true);
		Config.get().register(PREFERENCE_SIDEBAR_WIDTH, "Width (in cells) of the sidebar", 16, false, true);
		Config.get().register(PREFERENCE_MOUSE_SCROLL, "Use mouse to scroll game-map screen?", true, true, false);
	}
	
	public static final SColor NOT_VISIBLE_DARKNESS = SColor.INK;
	public static final float NOT_VISIBLE_DARKNESS_FLOAT = NOT_VISIBLE_DARKNESS.toFloatBits();
	
	private HorizontalGroup rootActor;
	private Viewport mapViewport, sidebarViewport;
	private SparseLayers mapGrid, sidebarGrid;
	private GameScreenInputProcessor inputProcessor;
	
	private float mapGridScreenCellWidth = -1, mapGridScreenCellHeight = -1;
	private int sidebarCellHeight = -1;
	
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
	
	private boolean isActiveScrollTo = false;
	private float scrollToX = 0, scrollToY = 0;
	
	private InputEventListener scrollLeftListener, scrollRightListener, scrollUpListener, scrollDownListener;
	
	public GameScreen() {
		
		super();
		
		rootActor = new HorizontalGroup() {
			
			@Override
			public void draw(Batch batch, float parentAlpha) {
				
				GameScreen.this.draw(batch, parentAlpha);
			}
		};
		
		final TextCellFactory font = new TextCellFactory().font(Fonts.get().get(Fonts.FONT_MAP));
		font.resetSize(getCellWidth(), getCellHeight());
		sidebarGrid = new SparseLayers(getSidebarCellWidth(), getSidebarCellHeight(), getCellWidth(), getCellHeight(),
				font);
		
		sidebarViewport = new ScalingViewport(Scaling.none, getSidebarPixelWidth(), getSidebarPixelHeight());
		sidebarViewport.setScreenBounds((int) (getWindowPixelWidth() - getSidebarPixelWidth()), 0,
				(int) getSidebarPixelWidth(), (int) getSidebarPixelHeight());
		
		for (int x = 0; x < getSidebarCellWidth(); x++)
			for (int y = 0; y < getSidebarCellHeight(); y++)
				sidebarGrid.put(x, y, (x + y) % 2 == 0 ? 'X' : ' ', SColor.WHITE_FLOAT_BITS);
			
		rootActor.setFillParent(true);
		
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
		
		//
		// If there is an active "scroll-to", then do that scrolling now.
		//
		if (isActiveScrollTo) {
			
			final float dx = (cameraX - scrollToX);
			final float dy = (cameraY - scrollToY);
			final float smallestDx = getCellWidth() * 8f, smallestDy = getCellHeight() * 8f;
			final float clampedDx = Math.max(Math.abs(dx), smallestDx) * Math.signum(dx);
			final float clampedDy = Math.max(Math.abs(dy), smallestDy) * Math.signum(dy);
			
			cameraX -= clampedDx * delta;
			cameraY -= clampedDy * delta;
			
			boolean dxDone = false, dyDone = false;
			
			if (Math.abs(dx) < getCellWidth() / 2f) {
				cameraX = scrollToX;
				dxDone = true;
			}
			
			if (Math.abs(dy) < getCellHeight() / 2f) {
				cameraY = scrollToY;
				dyDone = true;
			}
			
			if (dxDone && dyDone)
				isActiveScrollTo = false;
		}
	}
	
	private void draw(Batch batch, float parentAlpha) {
		
		final Viewport oldViewport;
		if (rootActor.getStage() != null) {
			oldViewport = rootActor.getStage().getViewport();
			batch.end();
			batch.begin();
		} else
			oldViewport = null;
		
		if (mapViewport != null && mapGrid != null) {
			
			mapViewport.getCamera().position.x = cameraX;
			mapViewport.getCamera().position.y = cameraY;
			
			mapViewport.apply(false);
			batch.setProjectionMatrix(mapViewport.getCamera().combined);
			mapGrid.font.configureShader(batch);
			
			if (rootActor.getStage() != null)
				rootActor.getStage().setViewport(mapViewport);
			
			mapGrid.draw(batch, parentAlpha);
			
			batch.end();
			batch.begin();
		}
		
		sidebarViewport.getCamera().position.x = (int) getSidebarPixelWidth() / 2f;
		sidebarViewport.getCamera().position.y = (int) getSidebarPixelHeight() / 2f;
		
		sidebarViewport.apply(false);
		batch.setProjectionMatrix(sidebarViewport.getCamera().combined);
		sidebarGrid.font.configureShader(batch);
		
		if (rootActor.getStage() != null)
			rootActor.getStage().setViewport(sidebarViewport);
		
		sidebarGrid.draw(batch, parentAlpha);
		
		if (rootActor.getStage() != null) {
			batch.end();
			batch.begin();
			rootActor.getStage().setViewport(oldViewport);
		}
	}
	
	/**
	 * @return the map-screen grid
	 */
	public SparseLayers getMapSurface() {
		
		return mapGrid;
	}
	
	/**
	 * @return the sidebar grid
	 */
	public SparseLayers getSidebarSurface() {
		
		return sidebarGrid;
	}
	
	/**
	 * Clears the {@link SparseLayers} map-grid. If its current size does not match
	 * the given size, removes and re-creates the SparseLayers instance to match.
	 * <p>
	 * This method will center the GameScreen's camera at the middle of the map.
	 * </p>
	 * 
	 * @param width
	 * @param height
	 */
	public void resizeSurface(int width, int height) {
		
		if (mapGrid != null && width == mapGrid.getGridWidth() && height == mapGrid.getGridHeight())
			mapGrid.clear();
		else {
			if (mapGrid != null)
				mapGrid.remove();
			
			final TextCellFactory font = new TextCellFactory().font(Fonts.get().get(Fonts.FONT_MAP));
			font.resetSize(getCellWidth(), getCellHeight());
			
			mapGrid = new SparseLayers(width, height, getCellWidth(), getCellHeight(), font);
			
			mapViewport = new ScreenViewport();
			mapViewport.setWorldSize((int) getMapGridOnscreenPixelWidth(), (int) getMapGridOnscreenPixelHeight());
			mapViewport.setScreenBounds(0, 0, (int) getMapGridOnscreenPixelWidth(),
					(int) getMapGridOnscreenPixelHeight());
			
			cursor = mapGrid.glyph('\u2588', SColor.multiplyAlpha(SColor.AURORA_CLOUD, 0.5f), 0, 0);
			
			getInputProcessor().resize(getCellWidth(), getCellHeight(), getMapGridWorldCellWidth(),
					getMapGridWorldCellHeight(), 0, 0);
		}
		
		cameraX = (width * getCellWidth()) / 2f;
		cameraY = (height * getCellHeight()) / 2f;
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
			inputProcessor = new GameScreenInputProcessor(0, 0, getCellWidth(), getCellHeight(),
					getMapGridWorldCellWidth(), getMapGridWorldCellHeight(), this);
			
			setupScrollHoverListeners();
			
			inputProcessor.registerInputListener(InputEventListener.build().continuous().onEvent((e) -> {
				if (cursor == null || mapGrid == null)
					return;
				
				final float x = mapGrid.worldX(e.getMapCursor().x);
				final float y = mapGrid.worldY(e.getMapCursor().y);
				cursor.setPosition(x, y);
			}).get());
			
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
		
		final float worldWidth = getMapGridWorldCellWidth() * getCellWidth(),
				worldHeight = getMapGridWorldCellHeight() * getCellHeight();
		
		final float minX = 0 + (getMapGridOnscreenPixelWidth() / 2f), minY = 0 + (getMapGridOnscreenPixelHeight() / 2f);
		final float maxX = worldWidth - getMapGridOnscreenPixelWidth() / 2f;
		final float maxY = worldHeight - getMapGridOnscreenPixelHeight() / 2f;
		
		cameraX = Math.min(Math.max(newX, minX), maxX);
		cameraY = Math.min(Math.max(newY, minY), maxY);
	}
	
	/**
	 * Set's the map-screen's "active-scroll-to" -- i.e., the map will automatically
	 * scroll every frame, eventually placing the given map-cell in the center of
	 * the display.
	 * 
	 * @param mapLocation
	 */
	public void setActiveScrollTo(Coord mapLocation) {
		
		if (getMapSurface() == null)
			return;
		final float centerOnPixelX = mapLocation.x * getCellWidth();
		final float centerOnPixelY = (getMapGridWorldCellHeight() - mapLocation.y) * getCellHeight();
		
		final float worldWidthPixels = getMapGridWorldCellWidth() * getCellWidth();
		final float worldHeightPixels = getMapGridWorldCellHeight() * getCellHeight();
		
		final float minX = 0 + (getMapGridOnscreenPixelWidth() / 2f), minY = 0 + (getMapGridOnscreenPixelHeight() / 2f);
		final float maxX = worldWidthPixels - (getMapGridOnscreenPixelWidth() / 2f),
				maxY = worldHeightPixels - (getMapGridOnscreenPixelHeight() / 2f);
		
		final float clampedCornerPixelX = Math.max(Math.min(centerOnPixelX, maxX), minX);
		final float clampedCornerPixelY = Math.max(Math.min(centerOnPixelY, maxY), minY);
		
		this.scrollToX = clampedCornerPixelX;
		this.scrollToY = clampedCornerPixelY;
		this.isActiveScrollTo = true;
	}
	
	/**
	 * Un-register any previously-registered and, if enabled in configuration,
	 * re-creates the scrolling {@link MouseHoverListener}s.
	 */
	private void setupScrollHoverListeners() {
		
		final int onscreenGridWidth = (int) (getWindowPixelWidth() / getCellWidth());
		final int onscreenGridHeight = (int) (getWindowPixelHeight() / getCellHeight());
		
		if (scrollLeftListener != null)
			getInputProcessor().unregisterInputListener(scrollLeftListener);
		if (scrollRightListener != null)
			getInputProcessor().unregisterInputListener(scrollRightListener);
		if (scrollUpListener != null)
			getInputProcessor().unregisterInputListener(scrollUpListener);
		if (scrollDownListener != null)
			getInputProcessor().unregisterInputListener(scrollDownListener);
		
		if (Config.get().getBoolean(PREFERENCE_MOUSE_SCROLL)) {
			
			final int bufferWidth = onscreenGridWidth / 8;
			final int bufferHeight = onscreenGridHeight / 8;
			
			scrollLeftListener = InputEventListener.build()
			//@formatter:off
							.continuous()
							.inWindow(Coord.get(0,0), Coord.get(bufferWidth,onscreenGridHeight))
							.onEvent(e -> doScroll(Direction.LEFT, bufferWidth - e.getScreenCursor().x))
					//@formatter:on
					.get();
			scrollRightListener = InputEventListener.build()
			//@formatter:off
							.continuous()
							.inWindow(Coord.get(onscreenGridWidth-bufferWidth,0), Coord.get(onscreenGridWidth,onscreenGridHeight))
							.onEvent(e -> doScroll(Direction.RIGHT, e.getScreenCursor().x - (onscreenGridWidth - bufferWidth) + 1))
					//@formatter:on
					.get();
			scrollUpListener = InputEventListener.build()
			//@formatter:off
							.continuous()
							.inWindow(Coord.get(0,0), Coord.get(onscreenGridWidth,bufferHeight))
							.onEvent(e -> doScroll(Direction.UP, bufferHeight - e.getScreenCursor().y))
					//@formatter:on
					.get();
			scrollDownListener = InputEventListener.build()
			//@formatter:off
							.continuous()
							.inWindow(Coord.get(0,onscreenGridHeight-bufferHeight), Coord.get(onscreenGridWidth,onscreenGridHeight))
							.onEvent(e -> doScroll(Direction.DOWN, e.getScreenCursor().y - (onscreenGridHeight - bufferHeight) + 1))
					//@formatter:on
					.get();
			
			getInputProcessor().registerInputListener(scrollLeftListener);
			getInputProcessor().registerInputListener(scrollRightListener);
			getInputProcessor().registerInputListener(scrollUpListener);
			getInputProcessor().registerInputListener(scrollDownListener);
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
		
		return mapGrid.addLayer(getNamedLayerIndex(name));
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
			freeIndex = mapGrid.getLayerCount();
		
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
		final SparseTextMap layer = mapGrid.getLayer(index);
		if (layer != null)
			layer.clear();
		
		layerNamesToIndices.remove(name);
		freeLayerIndices.add(index);
	}
	
	public float getWindowPixelWidth() {
		
		return Config.get().getInt(App.PREFERENCE_WINDOW_WIDTH);
	}
	
	public float getWindowPixelHeight() {
		
		return Config.get().getInt(App.PREFERENCE_WINDOW_HEIGHT);
	}
	
	public float getCellWidth() {
		
		return Config.get().getFloat(PREFERENCE_CELL_WIDTH);
	}
	
	public float getCellHeight() {
		
		return Config.get().getFloat(PREFERENCE_CELL_HEIGHT);
	}
	
	public float getSidebarPixelWidth() {
		
		return getSidebarCellWidth() * getCellWidth();
	}
	
	public float getSidebarPixelHeight() {
		
		return getSidebarCellHeight() * getCellHeight();
	}
	
	public int getSidebarCellWidth() {
		
		return Config.get().getInt(PREFERENCE_SIDEBAR_WIDTH);
	}
	
	public int getSidebarCellHeight() {
		
		if (sidebarCellHeight < 0)
			sidebarCellHeight = (int) (getWindowPixelHeight() / getCellHeight());
		return sidebarCellHeight;
	}
	
	public float getMapGridOnscreenCellWidth() {
		
		if (mapGridScreenCellWidth < 0)
			mapGridScreenCellWidth = getWindowPixelWidth() / getCellWidth() - getSidebarCellWidth();
		return mapGridScreenCellWidth;
	}
	
	public float getMapGridOnscreenCellHeight() {
		
		if (mapGridScreenCellHeight < 0)
			mapGridScreenCellHeight = getWindowPixelHeight() / getCellHeight();
		return mapGridScreenCellHeight;
	}
	
	public int getMapGridWorldCellWidth() {
		
		if (mapGrid != null)
			return mapGrid.getGridWidth();
		
		return 0;
	}
	
	public int getMapGridWorldCellHeight() {
		
		if (mapGrid != null)
			return mapGrid.getGridHeight();
		
		return 0;
	}
	
	public float getMapGridOnscreenPixelWidth() {
		
		return getMapGridOnscreenCellWidth() * getCellWidth();
	}
	
	public float getMapGridOnscreenPixelHeight() {
		
		return getMapGridOnscreenCellHeight() * getCellHeight();
	}
	
	@Override
	public int screenToMapX(int screenX) {
		
		if (mapGrid == null)
			return 0;
		
		final int leftGridCell = mapGrid.gridX(cameraX - getMapGridOnscreenPixelWidth() / 2f);
		return screenX + leftGridCell;
	}
	
	@Override
	public int screenToMapY(int screenY) {
		
		if (mapGrid == null)
			return 0;
		
		final int topGridCell = mapGrid.gridY(cameraY + getMapGridOnscreenPixelHeight() / 2f);
		return screenY + topGridCell;
	}
	
	@Override
	public int mapToScreenX(int mapX) {
		
		if (mapGrid == null)
			return 0;
		
		final int leftGridCell = mapGrid.gridX(cameraX - getMapGridOnscreenPixelWidth() / 2f);
		return mapX - leftGridCell;
	}
	
	@Override
	public int mapToScreenY(int mapY) {
		
		if (mapGrid == null)
			return 0;
		
		final int topGridCell = mapGrid.gridY(cameraY + getMapGridOnscreenPixelHeight() / 2f);
		return mapY - topGridCell;
	}
	
	@Override
	public void dispose() {
		
		EventBus.get().unregister(this);
	}
}
