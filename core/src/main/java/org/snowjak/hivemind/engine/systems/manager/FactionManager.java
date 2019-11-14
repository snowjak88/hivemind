/**
 * 
 */
package org.snowjak.hivemind.engine.systems.manager;

import org.snowjak.hivemind.Factions;
import org.snowjak.hivemind.Factions.Faction;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

/**
 * Manages {@link Entity}/faction membership.
 * <p>
 * All Entities belong to a faction. If an Entity is not assigned to a faction,
 * it is assumed to belong to the DEFAULT faction.
 * </p>
 * <p>
 * All factions have a relation-"score" with each other that determines how each
 * member of that faction interacts with the other.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class FactionManager extends EntitySystem {
	
	private final OrderedMap<Faction, OrderedSet<Entity>> factionToEntities = new OrderedMap<>();
	private final OrderedMap<Entity, Faction> entityToFaction = new OrderedMap<>();
	
	private final EntityListener defaultFactionListener;
	
	public FactionManager() {
		
		super();
		
		defaultFactionListener = new EntityListener() {
			
			@Override
			public void entityAdded(Entity entity) {
				
				synchronized (FactionManager.this) {
					set(Factions.get().getDefault(), entity);
				}
			}
			
			@Override
			public void entityRemoved(Entity entity) {
				
				synchronized (FactionManager.this) {
					final Faction oldFaction = entityToFaction.remove(entity);
					if (oldFaction != null && factionToEntities.get(oldFaction) != null)
						factionToEntities.get(oldFaction).remove(entity);
				}
			}
		};
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		
		engine.addEntityListener(defaultFactionListener);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		engine.removeEntityListener(defaultFactionListener);
		
		super.removedFromEngine(engine);
	}
	
	/**
	 * Sets the given {@link Entity} to be associated to the given {@link Faction}.
	 * <p>
	 * If either {@code faction} or {@code entity} are {@code null}, this method
	 * does nothing.
	 * </p>
	 * 
	 * @param faction
	 * @param entity
	 */
	public void set(Faction faction, Entity entity) {
		
		synchronized (this) {
			if (faction == null || entity == null)
				return;
			
			factionToEntities.computeIfAbsent(faction, x -> new OrderedSet<>()).add(entity);
			entityToFaction.put(entity, faction);
		}
	}
	
	/**
	 * Get the {@link Faction} associated with the given {@link Entity}, or
	 * {@code null} if no such association has been defined.
	 * 
	 * @param entity
	 * @return
	 */
	public Faction get(Entity entity) {
		
		synchronized (this) {
			return entityToFaction.get(entity);
		}
	}
	
	/**
	 * Get those {@link Entity Entities} associated with the given {@link Faction}.
	 * 
	 * @param faction
	 * @return
	 */
	public OrderedSet<Entity> get(Faction faction) {
		
		synchronized (this) {
			return factionToEntities.computeIfAbsent(faction, x -> new OrderedSet<>());
		}
	}
	
	/**
	 * Return {@code true} if this manager contains an association for the given
	 * Entity.
	 * 
	 * @param entity
	 * @return
	 */
	public boolean has(Entity entity) {
		
		synchronized (this) {
			return entityToFaction.containsKey(entity);
		}
	}
	
	/**
	 * Return {@code true} if this manager contains an association for the given
	 * Faction.
	 * 
	 * @param faction
	 * @return
	 */
	public boolean has(Faction faction) {
		
		synchronized (this) {
			return factionToEntities.containsKey(faction);
		}
	}
}
