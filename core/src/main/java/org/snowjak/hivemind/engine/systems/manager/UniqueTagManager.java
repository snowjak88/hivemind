/**
 * 
 */
package org.snowjak.hivemind.engine.systems.manager;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;

/**
 * A system which allows you to associate individual entities with tags, for
 * easy referral to important entities.
 * <p>
 * Note that, while any tag may refer to only one entity at a time, an entity
 * may be referred-to by multiple tags.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class UniqueTagManager extends EntitySystem implements EntityListener {
	
	private final Map<String, Entity> tagToEntity = new LinkedHashMap<>();
	private final Map<Entity, String> entityToTag = new LinkedHashMap<>();
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		engine.addEntityListener(this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener(this);
		super.removedFromEngine(engine);
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		// Nothing to do.
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		synchronized (this) {
			final String tag = entityToTag.remove(entity);
			if (tag != null)
				tagToEntity.remove(tag);
		}
	}
	
	/**
	 * @return the set of active tags
	 */
	public Set<String> getTags() {
		
		synchronized (this) {
			return tagToEntity.keySet();
		}
	}
	
	/**
	 * Associate the given Entity with the given tag.
	 * 
	 * @param tag
	 * @param entity
	 */
	public void set(String tag, Entity entity) {
		
		synchronized (this) {
			if (tag != null && entity != null) {
				tagToEntity.put(tag, entity);
				entityToTag.put(entity, tag);
			}
			else if (tag != null)
				unset(tag);
			
			else if (entity != null)
				unset(entity);
		}
	}
	
	/**
	 * Remove the association for the given tag from this manager.
	 * 
	 * @param tag
	 */
	public void unset(String tag) {
		
		synchronized (this) {
			final Entity e = tagToEntity.remove(tag);
			if (e != null)
				entityToTag.remove(e);
		}
	}
	
	/**
	 * Remove the association for the given Entity from this manager.
	 * 
	 * @param entity
	 */
	public void unset(Entity entity) {
		
		synchronized (this) {
			final String oldTag = entityToTag.remove(entity);
			if (oldTag != null)
				tagToEntity.remove(oldTag);
		}
	}
	
	/**
	 * @param tag
	 * @return {@code true} if this manager contains the given tag
	 */
	public boolean has(String tag) {
		
		synchronized (this) {
			return tagToEntity.containsKey(tag);
		}
	}
	
	/**
	 * @param entity
	 * @return {@code true} if this manager contains the given {@link Entity}
	 */
	public boolean has(Entity entity) {
		
		synchronized (this) {
			return entityToTag.containsKey(entity);
		}
	}
	
	/**
	 * Get the {@link Entity} associated with the given tag, or {@code null} if no
	 * such association has been created.
	 * 
	 * @param tag
	 * @return
	 */
	public Entity get(String tag) {
		
		synchronized (this) {
			return tagToEntity.get(tag);
		}
	}
	
	/**
	 * Get the tag associated with the given {@link Entity}, or {@code null} if no
	 * such association has been created.
	 * 
	 * @param entity
	 * @return
	 */
	public String get(Entity entity) {
		
		synchronized (this) {
			return entityToTag.get(entity);
		}
	}
}
