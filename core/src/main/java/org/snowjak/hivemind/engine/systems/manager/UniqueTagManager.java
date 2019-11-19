/**
 * 
 */
package org.snowjak.hivemind.engine.systems.manager;

import java.util.Set;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

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
	
	private final OrderedMap<String, Entity> tagToEntity = new OrderedMap<>();
	private final OrderedMap<Entity, OrderedSet<String>> entityToTag = new OrderedMap<>();
	
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
			final OrderedSet<String> tags = entityToTag.remove(entity);
			if (tags == null || tags.isEmpty())
				return;
			for (int i = 0; i < tags.size(); i++)
				tagToEntity.remove(tags.getAt(i));
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
	 * @param entity
	 */
	public void set(String tag, Entity entity) {
		
		synchronized (this) {
			if (tag != null && entity != null) {
				tagToEntity.put(tag, entity);
				entityToTag.computeIfAbsent(entity, x -> new OrderedSet<>()).add(tag);
			}
		}
	}
	
	public void unset(String tag, Entity entity) {
		
		synchronized (this) {
			if (tag != null && entity != null) {
				if (tagToEntity.get(tag) == entity) {
					tagToEntity.remove(tag);
					entityToTag.get(entity).remove(tag);
				}
			}
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
				entityToTag.computeIfAbsent(e, x -> new OrderedSet<>()).remove(tag);
		}
	}
	
	/**
	 * Disassociates the given Entity from all its tags and un-registers that Entity
	 * from this manager.
	 * 
	 * @param entity
	 */
	public void unset(Entity entity) {
		
		synchronized (this) {
			final OrderedSet<String> oldTags = entityToTag.remove(entity);
			if (oldTags == null || oldTags.isEmpty())
				return;
			for (int i = 0; i < oldTags.size(); i++)
				tagToEntity.remove(oldTags.getAt(i));
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
	 * Get the tags associated with the given {@link Entity}, or {@code null} if no
	 * such association has been created.
	 * 
	 * @param entity
	 * @return
	 */
	public OrderedSet<String> get(Entity entity) {
		
		synchronized (this) {
			return entityToTag.computeIfAbsent(entity, x -> new OrderedSet<>());
		}
	}
}
