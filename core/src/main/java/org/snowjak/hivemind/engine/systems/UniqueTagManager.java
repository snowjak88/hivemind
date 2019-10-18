/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.ashley.core.Entity;
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
public class UniqueTagManager extends EntitySystem {
	
	private final Map<String, Entity> tagToEntity = new LinkedHashMap<>();
	private final Map<Entity, String> entityToTag = new LinkedHashMap<>();
	
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
			tagToEntity.put(tag, entity);
			entityToTag.put(entity, tag);
		}
	}
	
	/**
	 * Remove the association for the given tag from this manager.
	 * 
	 * @param tag
	 */
	public void unset(String tag) {
		
		synchronized (this) {
			final Entity e = tagToEntity.get(tag);
			if (e != null) {
				entityToTag.remove(e);
				tagToEntity.remove(tag);
			}
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
		synchronized(this) {
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
