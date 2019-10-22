/**
 * 
 */
package org.snowjak.hivemind.engine;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;

import squidpony.squidmath.OrderedMap;

/**
 * Repository for cached {@link ComponentMapper} instances.
 * 
 * @author snowjak88
 *
 */
public class ComponentMappers {
	
	private static ComponentMappers __INSTANCE = null;
	
	public static ComponentMappers get() {
		
		if (__INSTANCE == null)
			synchronized (ComponentMappers.class) {
				if (__INSTANCE == null)
					__INSTANCE = new ComponentMappers();
			}
		return __INSTANCE;
	}
	
	private final OrderedMap<Class<? extends Component>, ComponentMapper<?>> instances = new OrderedMap<>();
	
	private ComponentMappers() {
		
	}
	
	/**
	 * Get the cached {@link ComponentMapper} instance for the given
	 * {@link Component}-type.
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Component> ComponentMapper<T> get(Class<T> clazz) {
		
		return (ComponentMapper<T>) instances.computeIfAbsent(clazz, c -> ComponentMapper.getFor(c));
	}
}
