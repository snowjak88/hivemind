/**
 * 
 */
package org.snowjak.hivemind.util;

import java.util.EnumMap;

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
	
	private final EnumMap<SpatialOperation, OrderedSet<T>> recentUpdates = new EnumMap<>(SpatialOperation.class);
	private final OrderedSet<T> recentlyUpdated = new OrderedSet<>();
	
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
				
			if (location == null) {
				if (prevLocation != null) {
					recentlyUpdated.add(value);
					recentUpdates.computeIfAbsent(SpatialOperation.REMOVED, x -> new OrderedSet<>()).add(value);
				}
				return;
			}
			
			coordToObjects.computeIfAbsent(location, x -> new OrderedSet<>()).add(value);
			objectToCoord.put(value, location);
			
			recentlyUpdated.add(value);
			if (prevLocation == null)
				recentUpdates.computeIfAbsent(SpatialOperation.ADDED, x -> new OrderedSet<>()).add(value);
			else
				recentUpdates.computeIfAbsent(SpatialOperation.MOVED, x -> new OrderedSet<>()).add(value);
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
				recentlyUpdated.add(value);
				recentUpdates.computeIfAbsent(SpatialOperation.REMOVED, x -> new OrderedSet<>()).add(value);
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
				remove(values.getAt(i));
			
			return values;
		}
	}
	
	/**
	 * Reset this map. Leaves no record behind for
	 * {@link #getRecentlyUpdated(SpatialOperation) getRecentlyUpdated(REMOVE)}
	 */
	public void clear() {
		
		synchronized (this) {
			coordToObjects.clear();
			objectToCoord.clear();
			recentlyUpdated.clear();
			recentUpdates.clear();
		}
	}
	
	/**
	 * Remove all references to the given value from this map (including all
	 * {@link #getRecentlyUpdated() "recently-updated"} records). This is really
	 * intended if you need to discard all references to an instance because, e.g.,
	 * you need to recycle it into an instance-pool.
	 * 
	 * @param value
	 */
	protected void hardRemove(T value) {
		
		synchronized (this) {
			final Coord c = objectToCoord.remove(value);
			if (c != null)
				coordToObjects.get(c).remove(value);
			recentlyUpdated.remove(value);
			recentUpdates.forEach((op, s) -> s.remove(value));
		}
	}
	
	/**
	 * Get a list of values that have been updated (whether added, moved, or
	 * removed) since the last call to {@link #resetRecentlyUpdated()}.
	 * 
	 * @return
	 */
	public OrderedSet<T> getRecentlyUpdated() {
		
		return recentlyUpdated;
	}
	
	/**
	 * Get the list of values that have been updated with the given
	 * {@link SpatialOperation}-type since the last call to
	 * {@link #resetRecentlyUpdated()}.
	 * 
	 * @param operation
	 * @return
	 */
	public OrderedSet<T> getRecentlyUpdated(SpatialOperation operation) {
		
		synchronized (this) {
			return recentUpdates.computeIfAbsent(operation, x -> new OrderedSet<>());
		}
	}
	
	/**
	 * Remove the given value from the internal record of "recently-updated".
	 * 
	 * @param value
	 * @see #resetRecentlyUpdated() to reset all recently-updated
	 */
	public void resetRecentlyUpdated(T value) {
		
		synchronized (this) {
			recentlyUpdated.remove(value);
			recentUpdates.forEach((op, s) -> s.remove(value));
		}
	}
	
	/**
	 * Remove the given value from the internal record of "recently-updated" for the
	 * give {@link SpatialOperation}-type.
	 * 
	 * @param operation
	 * @param value
	 */
	public void resetRecentlyUpdated(SpatialOperation operation, T value) {
		
		synchronized (this) {
			if (recentUpdates.get(operation) != null)
				recentUpdates.get(operation).remove(value);
			
			boolean anyStillExist = false;
			for (int i = 0; i < SpatialOperation.values().length; i++) {
				final SpatialOperation op = SpatialOperation.values()[i];
				if (recentUpdates.get(op) != null && recentUpdates.get(op).contains(value)) {
					anyStillExist = true;
					break;
				}
			}
			
			if (!anyStillExist)
				recentlyUpdated.remove(value);
		}
	}
	
	/**
	 * Reset the list of recently-updated values.
	 * 
	 * @see #resetRecentlyUpdated(Object) to reset for only one value
	 */
	public void resetRecentlyUpdated() {
		
		synchronized (this) {
			recentlyUpdated.clear();
			recentUpdates.forEach((op, v) -> v.clear());
		}
	}
	
	public enum SpatialOperation {
		ADDED,
		MOVED,
		REMOVED
	}
}
