/**
 * 
 */
package org.snowjak.hivemind.engine.components;

import java.util.concurrent.Semaphore;

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
	private Semaphore lock = new Semaphore(1);
	
	public DijkstraMap getPathfinder() {
		
		return pathfinder;
	}
	
	public void setPathfinder(DijkstraMap pathfinder) {
		
		this.pathfinder = pathfinder;
	}
	
	public Semaphore getLock() {
		
		return lock;
	}
	
	@Override
	public void reset() {
		
		pathfinder = null;
		lock = new Semaphore(1);
	}
}
