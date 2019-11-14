/**
 * 
 */
package org.snowjak.hivemind.behavior

import java.util.concurrent.Callable
import java.util.concurrent.Future

import org.snowjak.hivemind.Context
import org.snowjak.hivemind.concurrent.Executor
import org.snowjak.hivemind.engine.Tags
import org.snowjak.hivemind.engine.components.HasMap
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.badlogic.gdx.ai.btree.Task.Status

import squidpony.squidmath.Coord
import squidpony.squidmath.GreasedRegion
import squidpony.squidmath.OrderedSet

/**
 * Allows you to create a LeafTask using a Closure, and provides a number of functions
 * to ease development.
 * 
 * @author snowjak88
 *
 */
class BehaviorCustomLeafTask extends LeafTask<Entity> {
	
	private Closure exec
	public prop = new HashMap<>()
	
	public BehaviorCustomLeafTask(Closure exec) {
		super()
		
		this.exec = exec
		this.exec.delegate = this
	}
	
	@Override
	public Status execute() {
		if(getObject() == null)
			return Status.FAILED
		
		exec(getObject())
	}
	
	@Override
	protected Task<Entity> copyTo(Task<Entity> task) {
		if(task instanceof BehaviorCustomLeafTask) {
			task.exec = this.exec
			task.exec.delegate = task
		}
		
		task
	}
	
	public <T extends Component> T create(Class<T> clazz) {
		if(getObject() == null)
			return null
		
		def c = Context.getEngine().createComponent(clazz)
		getObject().add c
		c
	}
	
	public boolean has(Class<? extends Component> clazz) {
		getObject() != null && ComponentMapper.getFor(clazz).has(getObject())
	}
	
	public <T extends Component> T get(Class<T> clazz) {
		(getObject() == null) ? null : ComponentMapper.getFor(clazz).get(getObject())
	}
	
	public void remove(Class<? extends Component> clazz) {
		if(getObject() != null)
			getObject().remove clazz
	}
	
	public HasMap worldMap() {
		def worldEntity = Context.getEngine().getSystem(UniqueTagManager).get(Tags.WORLD_MAP)
		if(worldEntity == null || !ComponentMapper.getFor(HasMap).has(worldEntity))
			return null
		ComponentMapper.getFor(HasMap).get(worldEntity)
	}
	
	public HasMap screenMap() {
		def screenEntity = Context.getEngine().getSystem(UniqueTagManager).get(Tags.SCREEN_MAP)
		if(screenEntity == null || !ComponentMapper.getFor(HasMap).has(screenEntity))
			return null
		ComponentMapper.getFor(HasMap).get(screenEntity)
	}
	
	public <T> OrderedSet<T> filterBy(OrderedSet<T> set, Closure filter) {
		
		def result = new OrderedSet<T>()
		for(int i : (0..<set.size())) {
			def v = set.getAt(i)
			
			if(filter(v))
				result.add v
		}
		
		result
	}
	
	public OrderedSet<Entity> entitiesIn(GreasedRegion region) {
		screenMap()?.entities?.getWithin(region) ?: new OrderedSet<>()
	}
	
	public OrderedSet<Entity> entitiesIn(GreasedRegion region, Closure filter) {
		filterBy(entitiesIn(region, filter))
	}
	
	public OrderedSet<Entity> entitiesAt(Coord location) {
		screenMap()?.entities?.getAt(location) ?: new OrderedSet<>()
	}
	
	public OrderedSet<Entity> entitiesAt(Coord location, Closure filter) {
		filterBy(entitiesAt(location), filter)
	}
	
	public Future<?> schedule(Closure task) {
		Executor.get().submit(task as Callable<?>)
	}
}
