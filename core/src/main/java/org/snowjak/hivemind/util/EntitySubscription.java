/**
 * 
 */
package org.snowjak.hivemind.util;

import java.util.function.Consumer;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.Family;

import squidpony.squidmath.OrderedSet;

/**
 * Allows you to maintain a list of active {@link Entity Entities} in an
 * {@link Engine} that match a certain {@link Family}.
 * <p>
 * Note that it is encouraged that you use the methods provided here to
 * {@link #registerWith(Engine) register} and {@link #unregisterWith(Engine)
 * un-register} an EntitySubscrition with an Engine.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class EntitySubscription implements EntityListener {
	
	private final OrderedSet<Entity> entities = new OrderedSet<>();
	private final Family family;
	private final Consumer<Entity> addAction, removeAction;
	
	/**
	 * Construct a new EntitySubscription for {@link Entity Entities} matching the
	 * given {@link Family}.
	 * 
	 * @param family
	 */
	public EntitySubscription(Family family) {
		
		this(family, null, null);
	}
	
	/**
	 * Construct a new EntitySubscription for {@link Entity Entities} matching the
	 * given {@link Family}. The given {@code addAction} and {@code removeAction}
	 * are optional operations that are executed every time an Entity is added to or
	 * removed from this subscription.
	 * 
	 * @param family
	 * @param addAction
	 *            action to perform when an Entity is added, or {@code null} for no
	 *            action
	 * @param removeAction
	 *            action to perform when an Entity is removed, or {@code null} for
	 *            no action
	 */
	public EntitySubscription(Family family, Consumer<Entity> addAction, Consumer<Entity> removeAction) {
		
		this.family = family;
		this.addAction = addAction;
		this.removeAction = removeAction;
	}
	
	/**
	 * Register this EntitySubscription with the given {@link Engine}.
	 * 
	 * @param engine
	 */
	public void registerWith(Engine engine) {
		
		synchronized (this) {
			engine.addEntityListener(getFamily(), this);
		}
	}
	
	/**
	 * Un-register this EntitySubscription from the given {@link Engine}.
	 * 
	 * @param engine
	 */
	public void unregisterWith(Engine engine) {
		
		synchronized (this) {
			engine.removeEntityListener(this);
		}
	}
	
	public Family getFamily() {
		
		return family;
	}
	
	public OrderedSet<Entity> getEntities() {
		
		synchronized (this) {
			return entities;
		}
	}
	
	@Override
	public void entityAdded(Entity entity) {
		
		synchronized (this) {
			if (entity != null) {
				entities.add(entity);
				if (addAction != null)
					addAction.accept(entity);
			}
		}
	}
	
	@Override
	public void entityRemoved(Entity entity) {
		
		synchronized (this) {
			if (entity != null) {
				entities.remove(entity);
				if (removeAction != null)
					removeAction.accept(entity);
			}
		}
	}
}
