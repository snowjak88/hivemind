/**
 * 
 */
package org.snowjak.hivemind.util;

import squidpony.squidmath.Coord;
import squidpony.squidmath.OrderedMap;
import squidpony.squidmath.OrderedSet;

/**
 * GameMap which associates {@link Coord locations} with potentially-multiple
 * objects.
 * 
 * @author snowjak88
 * @param <T>
 *            the object-type held in this SpatialMap
 */
public class SpatialMap<T> {
	
	private final OrderedMap<Coord, OrderedSet<T>> coordToObjects = new OrderedMap<>();
	private final OrderedMap<T, Coord> objectToCoord = new OrderedMap<>();
	
	/**
	 * Get the set of objects held at the given {@link Coord location}.
	 * 
	 * @param location
	 * @return
	 */
	public OrderedSet<T> getAt(Coord location) {
		
		synchronized (this) {
			return coordToObjects.computeIfAbsent(location, x -> new OrderedSet<>());
		}
	}
	
	/**
	 * Get the location associated with the given value, or {@code null} if this
	 * value has no such association.
	 * 
	 * @param value
	 * @return
	 */
	public Coord getLocation(T value) {
		
		synchronized (this) {
			return objectToCoord.get(value);
		}
	}
	
	/**
	 * Get all locations registered within this map (whether or not they have any
	 * values associated with them).
	 * 
	 * @return
	 */
	public OrderedSet<Coord> getLocations() {
		
		synchronized (this) {
			return coordToObjects.keysAsOrderedSet();
		}
	}
	
	/**
	 * Get all values associated with locations.
	 * 
	 * @return
	 */
	public OrderedSet<T> getValues() {
		
		synchronized (this) {
			return objectToCoord.keysAsOrderedSet();
		}
	}
	
	/**
	 * Sets this value's associated location. If the value is already associated
	 * with a different location, removes the previous association.
	 * 
	 * @param location
	 *            if {@code null}, removes the value from this {@link SpatialMap}
	 * @param value
	 */
	public void set(Coord location, T value) {
		
		synchronized (this) {
			final Coord prevLocation = objectToCoord.get(value);
			if (prevLocation != null)
				if (prevLocation == location)
					return;
				else
					coordToObjects.get(prevLocation).remove(value);
				
			if (location == null)
				return;
			
			coordToObjects.computeIfAbsent(location, x -> new OrderedSet<>()).add(value);
			objectToCoord.put(value, location);
		}
	}
	
	/**
	 * Remove the given value from this {@link SpatialMap}.
	 * 
	 * @param value
	 * @return the value's most-recently-associated location, or {@code null} if no
	 *         such association
	 */
	public Coord remove(T value) {
		
		synchronized (this) {
			final Coord location = objectToCoord.get(value);
			if (location != null) {
				objectToCoord.remove(value);
				coordToObjects.get(location).remove(value);
			}
			return location;
		}
	}
	
	/**
	 * Remove all values associated with the given location.
	 * 
	 * @param location
	 * @return
	 */
	public OrderedSet<T> removeAll(Coord location) {
		
		synchronized (this) {
			final OrderedSet<T> values = coordToObjects.get(location);
			if (values == null)
				return new OrderedSet<>();
			
			for (int i = 0; i < values.size(); i++)
				objectToCoord.remove(values.getAt(i));
			
			return values;
		}
	}
	
	/**
	 * Remove all locations and values held in this map.
	 */
	public void clear() {
		
		synchronized (this) {
			coordToObjects.clear();
			objectToCoord.clear();
		}
	}
}
