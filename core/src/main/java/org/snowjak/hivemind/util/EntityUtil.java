/**
 * 
 */
package org.snowjak.hivemind.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.Engine;
import org.snowjak.hivemind.engine.systems.manager.EntityRefManager;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

import squidpony.squidmath.OrderedMap;

/**
 * @author snowjak88
 *
 */
public class EntityUtil {
	
	private static final Logger LOG = Logger.getLogger(EntityUtil.class.getName());
	
	/**
	 * Clone the given {@link Entity}. For every {@link Component} associated with
	 * {@code toClone}, associates a new Component of the same type, and copies all
	 * properties (accessed via getters/setters) and fields (accessed directly).
	 * <p>
	 * This method will <em>not</em> register the returned Entity with any managers
	 * (e.g., {@link EntityRefManager}).
	 * </p>
	 * <p>
	 * This method will <em>not</em> add the returned Entity to the active
	 * {@link Engine}.
	 * </p>
	 * 
	 * @param toClone
	 * @return
	 */
	public static Entity clone(Entity toClone) {
		
		final Entity cloned = Context.getEngine().createEntity();
		
		try {
			
			for (Component toCloneComponent : toClone.getComponents()) {
				
				final Component clonedComponent = Context.getEngine().createComponent(toCloneComponent.getClass());
				
				//
				// We need to discover all property getters/setters.
				//
				
				//
				// Compile a list of all method-names which begin with "get" or "set"
				//
				
				final OrderedMap<String, Class<?>> getters = new OrderedMap<>(), setters = new OrderedMap<>();
				for (Method m : toCloneComponent.getClass().getMethods()) {
					if (m.getName().startsWith("get"))
						getters.put(m.getName().substring(3), m.getReturnType());
					else if (m.getName().startsWith("set") && m.getParameterCount() == 1)
						setters.put(m.getName().substring(3), m.getParameters()[0].getType());
				}
				
				//
				// Match up "get"-ers with "set"-ers, to discover properties.
				//
				
				final OrderedMap<String, Class<?>> propertyNames = new OrderedMap<>();
				for (String name : getters.keysAsOrderedSet())
					if (getters.get(name) != null && setters.get(name) != null)
						if (setters.get(name).isAssignableFrom(getters.get(name)))
							propertyNames.put(name, getters.get(name));
							
				//
				// Execute getter/setter pairs.
				//
				
				for (String propertyName : propertyNames.keySet()) {
					
					final Method getter = toCloneComponent.getClass().getMethod("get" + propertyName);
					final Method setter = toCloneComponent.getClass().getMethod("set" + propertyName,
							propertyNames.get(propertyName));
					
					setter.invoke(clonedComponent, getter.invoke(toCloneComponent));
				}
				
				//
				// We should also copy values for publicly-accessible fields.
				//
				
				for (Field field : toCloneComponent.getClass().getFields())
					field.set(clonedComponent, field.get(toCloneComponent));
				
				cloned.add(clonedComponent);
			}
		} catch (Throwable t) {
			LOG.severe("Cannot clone entity: " + t.getClass().getSimpleName() + ": " + t.getMessage());
			throw new RuntimeException("Cannot clone entity.", t);
		}
		
		return cloned;
	}
}
