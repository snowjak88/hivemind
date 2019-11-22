/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import java.util.concurrent.locks.ReentrantLock;

import org.snowjak.hivemind.util.loaders.IgnoreSerialization;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidai.DijkstraMap;

/**
 * Used to cache {@link DijkstraMap} instances for {@link Entity Entities} that
 * require them.
 * 
 * @author snowjak88
 *
 */
@IgnoreSerialization
public class HasPathfinder implements Component, Poolable {
	
	private DijkstraMap pathfinder;
	private final ReentrantLock lock = new ReentrantLock();
	
	public DijkstraMap getPathfinder() {
		
		return pathfinder;
	}
	
	public void setPathfinder(DijkstraMap pathfinder) {
		
		this.pathfinder = pathfinder;
	}
	
	public ReentrantLock getLock() {
		
		return lock;
	}
	
	@Override
	public void reset() {
		
		if (lock.isLocked())
			throw new IllegalStateException(
					"You cannot retire a HasPathfinder component before ensuring that its lock is released!");
		pathfinder = null;
	}
}
