/**
 * 
 */
package org.snowjak.hivemind.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.function.Function;

import org.snowjak.hivemind.App;
import org.snowjak.hivemind.engine.systems.BehaviorProcessingSystem;
import org.snowjak.hivemind.engine.systems.EntityRefManager;
import org.snowjak.hivemind.engine.systems.FOVCopyingSystem;
import org.snowjak.hivemind.engine.systems.FOVResettingSystem;
import org.snowjak.hivemind.engine.systems.FOVUpdatingSystem;
import org.snowjak.hivemind.engine.systems.GameScreenUpdatingSystem;
import org.snowjak.hivemind.engine.systems.LocationUpdatingSystem;
import org.snowjak.hivemind.engine.systems.MapScramblingSystem;
import org.snowjak.hivemind.engine.systems.OwnMapFOVInsertingSystem;
import org.snowjak.hivemind.engine.systems.PathfinderUpdatingSystem;
import org.snowjak.hivemind.engine.systems.RunnableExecutingSystem;
import org.snowjak.hivemind.engine.systems.UniqueTagManager;
import org.snowjak.hivemind.engine.systems.UpdatedLocationResettingSystem;
import org.snowjak.hivemind.json.Json;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

/**
 * Singleton object providing an interface to the
 * {@link com.badlogic.ashley.core.Engine entity engine}.
 * 
 * @author snowjak88
 *
 */
public class Engine {
	
	private static final Function<Integer, String> PREFAB_FILE_NAME = (id) -> App.class.getPackage().getName()
			+ File.separator + "prefab" + File.separator + "world_" + id + ".json";
	private static final String SAVE_FILE_NAME = App.class.getPackage().getName() + File.separator + "save"
			+ File.separator + "world.json";
	
	private PooledEngine engine;
	
	public Engine() {
		
		this.engine = new PooledEngine();
		
		this.engine.addSystem(new UpdatedLocationResettingSystem());
		this.engine.addSystem(new FOVResettingSystem());
		this.engine.addSystem(new LocationUpdatingSystem());
		this.engine.addSystem(new RunnableExecutingSystem());
		this.engine.addSystem(new MapScramblingSystem());
		
		this.engine.addSystem(new PathfinderUpdatingSystem());
		this.engine.addSystem(new FOVUpdatingSystem());
		this.engine.addSystem(new FOVCopyingSystem());
		this.engine.addSystem(new OwnMapFOVInsertingSystem());
		this.engine.addSystem(new BehaviorProcessingSystem());
		this.engine.addSystem(new GameScreenUpdatingSystem());
		
		this.engine.addSystem(new UniqueTagManager());
		this.engine.addSystem(new EntityRefManager());
	}
	
	/**
	 * Test whether the save-file (as defined by {@link #SAVE_FILE_NAME}) is
	 * available.
	 * 
	 * @return {@code true} if the save-file exists
	 */
	public boolean canLoad() {
		
		if (Gdx.files == null)
			throw new IllegalStateException("LibGDX is not yet initialized!");
		return Gdx.files.external(SAVE_FILE_NAME).exists();
	}
	
	/**
	 * Clears this Engine's state and loads its saved state from its save-file. If
	 * the load fails for any reason, an IOException is thrown. In the event of a
	 * failure, this Engine's state is indeterminate.
	 * 
	 * @throws IOException
	 *             if this Engine's save-file couldn't be loaded
	 */
	public void load() throws IOException {
		
		try {
			
			final FileHandle saveFile = Gdx.files.external(SAVE_FILE_NAME);
			
			try (Reader fr = saveFile.reader()) {
				
				clear();
				
				Json.get().fromJson(fr, Engine.class);
			}
			
		} catch (Throwable t) {
			throw new IOException("Could not load world!", t);
		}
	}
	
	/**
	 * Clear this Engine's state and load the prefab-state identified by {@code id}.
	 * If the load fails for any reason, an {@link IOException} is thrown. In the
	 * event of a failure, this Engine's state is indeterminate.
	 * 
	 * @param id
	 * @throws IOException
	 *             if this Engine's prefab-state couldn't be loaded
	 */
	public void loadPrefab(int id) throws IOException {
		
		try {
			final String saveFilename = PREFAB_FILE_NAME.apply(id);
			final FileHandle saveFile = Gdx.files.external(saveFilename);
			if (!saveFile.exists())
				throw new FileNotFoundException("PrefabBitmapFonts '" + saveFilename + "' not found.");
			
			clear();
			
			try (Reader fr = saveFile.reader()) {
				Json.get().fromJson(fr, Engine.class);
			}
		} catch (Throwable t) {
			throw new IOException("Could not load prefab world!", t);
		}
	}
	
	/**
	 * Save this Engine's current state to its save-file, overwriting whatever was
	 * there before.
	 * 
	 * @throws IOException
	 *             if this Engine's current-state couldn't be saved
	 */
	public void save() throws IOException {
		
		try {
			
			final FileHandle saveFile = Gdx.files.external(SAVE_FILE_NAME);
			
			try (Writer fw = saveFile.writer(false)) {
				Json.get().toJson(this, fw);
			}
			
		} catch (Throwable t) {
			throw new IOException("Could not save world!", t);
		}
	}
	
	/**
	 * If the Engine's save-file exists, delete it permanently. If it doesn't exist,
	 * this does nothing.
	 */
	public void removeSave() {
		
		final FileHandle saveFile = Gdx.files.external(SAVE_FILE_NAME);
		if (saveFile.exists())
			saveFile.delete();
	}
	
	/**
	 * Clears this Engine's current state -- removing all active {@link Entity
	 * Entities} along with their {@link Component Components}.
	 */
	public void clear() {
		
		engine.removeAllEntities();
		
		engine.clearPools();
	}
	
	/**
	 * @return
	 * @see com.badlogic.ashley.core.PooledEngine#createEntity()
	 */
	public Entity createEntity() {
		
		return engine.createEntity();
	}
	
	/**
	 * @param entity
	 * @see com.badlogic.ashley.core.Engine#addEntity(com.badlogic.ashley.core.Entity)
	 */
	public void addEntity(Entity entity) {
		
		engine.addEntity(entity);
	}
	
	/**
	 * @param <T>
	 * @param componentType
	 * @return
	 * @see com.badlogic.ashley.core.PooledEngine#createComponent(java.lang.Class)
	 */
	public <T extends Component> T createComponent(Class<T> componentType) {
		
		return engine.createComponent(componentType);
	}
	
	/**
	 * @param entity
	 * @see com.badlogic.ashley.core.Engine#removeEntity(com.badlogic.ashley.core.Entity)
	 */
	public void removeEntity(Entity entity) {
		
		engine.removeEntity(entity);
	}
	
	/**
	 * @return
	 * @see com.badlogic.ashley.core.Engine#getEntities()
	 */
	public ImmutableArray<Entity> getEntities() {
		
		return engine.getEntities();
	}
	
	/**
	 * @param <T>
	 * @param systemType
	 * @return
	 * @see com.badlogic.ashley.core.Engine#getSystem(java.lang.Class)
	 */
	public <T extends EntitySystem> T getSystem(Class<T> systemType) {
		
		return engine.getSystem(systemType);
	}
	
	/**
	 * @param family
	 * @return
	 * @see com.badlogic.ashley.core.Engine#getEntitiesFor(com.badlogic.ashley.core.Family)
	 */
	public ImmutableArray<Entity> getEntitiesFor(Family family) {
		
		return engine.getEntitiesFor(family);
	}
	
	/**
	 * @param listener
	 * @see com.badlogic.ashley.core.Engine#addEntityListener(com.badlogic.ashley.core.EntityListener)
	 */
	public void addEntityListener(EntityListener listener) {
		
		engine.addEntityListener(listener);
	}
	
	/**
	 * @param family
	 * @param listener
	 * @see com.badlogic.ashley.core.Engine#addEntityListener(com.badlogic.ashley.core.Family,
	 *      com.badlogic.ashley.core.EntityListener)
	 */
	public void addEntityListener(Family family, EntityListener listener) {
		
		engine.addEntityListener(family, listener);
	}
	
	/**
	 * @param listener
	 * @see com.badlogic.ashley.core.Engine#removeEntityListener(com.badlogic.ashley.core.EntityListener)
	 */
	public void removeEntityListener(EntityListener listener) {
		
		engine.removeEntityListener(listener);
	}
	
	/**
	 * @param deltaTime
	 * @see com.badlogic.ashley.core.Engine#update(float)
	 */
	public void update(float deltaTime) {
		
		engine.update(deltaTime);
	}
	
}
