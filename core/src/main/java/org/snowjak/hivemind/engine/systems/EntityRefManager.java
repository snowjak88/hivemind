/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.logging.Logger;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;

import squidpony.squidmath.SquidID;

/**
 * Manager for mapping unique-{@link SquidID ID}s to {@link Entity Entities} in
 * such a way that references can survive persistence.
 * <p>
 * This manager provides a method for deferring such reference-resolution. When
 * resuming an {@link Engine}'s state, you will necessarily need to recreate a
 * number of Entities. Certain {@link Component}s will refer to these Entities,
 * potentially before they are instantiated. Accordingly, you should:
 * <ol>
 * <li>When instantiating your Component, call
 * {@link EntityRefManager#addReferenceResolution(SquidID, Consumer)}, providing
 * the {@link SquidID ID} you need to resolve along with a {@link Consumer}
 * which will consume the resolved reference (e.g., to provide to the
 * Component)</li>
 * <li>Once your Entities are completely loaded, call
 * {@link EntityRefManager#resolveReferences()}, which will attempt to execute
 * all reference-resolvers you have provided</li>
 * </ol>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class EntityRefManager extends EntitySystem {
	
	private static final Logger LOG = Logger.getLogger(EntityRefManager.class.getName());
	
	private final BiMap<SquidID, Entity> references = Maps.synchronizedBiMap(HashBiMap.create());
	
	private final Map<SquidID, Queue<Consumer<Entity>>> referenceResolvers = Collections
			.synchronizedMap(new LinkedHashMap<>());
	
	/**
	 * Add an {@link Entity} to this ref-manager, associated it with a new random
	 * {@link SquidID ID}.
	 * 
	 * @param entity
	 */
	public void add(Entity entity) {
		
		references.put(SquidID.randomUUID(), entity);
	}
	
	/**
	 * Add an {@link Entity} to this ref-manager, associating it with the given
	 * {@link SquidID ID}.
	 * 
	 * @param entity
	 * @param id
	 */
	public void add(Entity entity, SquidID id) {
		
		references.put(id, entity);
	}
	
	/**
	 * Get the {@link Entity} associated with the given {@link SquidID ID}, or
	 * {@code null} if no such ID has been registered yet.
	 * 
	 * @param id
	 * @return
	 */
	public Entity get(SquidID id) {
		
		return references.get(id);
	}
	
	/**
	 * Get the {@link SquidID ID} associated with the given {@link Entity}.
	 * <p>
	 * If no ID is associated with this Entity, then this method will generate a
	 * random ID.
	 * </p>
	 * 
	 * @param entity
	 * @return
	 */
	public SquidID get(Entity entity) {
		
		return references.inverse().computeIfAbsent(entity, e -> SquidID.randomUUID());
	}
	
	/**
	 * Check if the given {@link Entity} has an associated {@link SquidID ID}.
	 * 
	 * @param entity
	 * @return
	 */
	public boolean has(Entity entity) {
		
		return references.containsValue(entity);
	}
	
	/**
	 * Check if the given {@link SquidID ID} has been associated with any
	 * {@link Entity}.
	 * 
	 * @param id
	 * @return
	 */
	public boolean has(SquidID id) {
		
		return references.containsKey(id);
	}
	
	/**
	 * Provide a reference-resolver -- a method of deferred resolution to an Entity
	 * reference.
	 * 
	 * @param idToResolve
	 * @param resolution
	 * @see #resolveReferences()
	 */
	public void addReferenceResolution(SquidID idToResolve, Consumer<Entity> resolution) {
		
		synchronized (this) {
			referenceResolvers.computeIfAbsent(idToResolve, id -> new LinkedBlockingQueue<>()).add(resolution);
		}
	}
	
	/**
	 * Attempt to execute all {@link #addReferenceResolution(SquidID, Consumer)
	 * added reference-resolvers}. All successful resolvers are removed from this
	 * manager. All unsuccessful resolvers are retained.
	 * 
	 * @return {@code true} if all reference-resolvers were successful,
	 *         {@code false} if any were unsuccessful
	 */
	public boolean resolveReferences() {
		
		synchronized (this) {
			boolean allSuccessful = true;
			
			final Collection<SquidID> successfullyResolved = new LinkedList<>();
			
			for (SquidID id : referenceResolvers.keySet())
				if (references.containsKey(id)) {
					
					for (Consumer<Entity> resolver : referenceResolvers.get(id)) {
						try {
							resolver.accept(references.get(id));
						} catch (Throwable t) {
							LOG.severe("Unexpected exception while resolving a reference to entity (ID = [" + id.a + "/"
									+ id.b + "/" + id.c + "/" + id.d + "])");
							t.printStackTrace(System.err);
							allSuccessful = false;
						}
					}
					
					successfullyResolved.add(id);
					
				} else {
//					LOG.severe("Cannot resolve reference to entity (ID = [" + id.a + "/" + id.b + "/" + id.c + "/"
//							+ id.d + "])");
					allSuccessful = false;
				}
			
			successfullyResolved.forEach(id -> referenceResolvers.remove(id));
			
			return allSuccessful;
		}
	}
}