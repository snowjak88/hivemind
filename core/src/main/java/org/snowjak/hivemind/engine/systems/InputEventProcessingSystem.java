/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.snowjak.hivemind.events.EventPool;
import org.snowjak.hivemind.events.input.InputEvent;

import com.badlogic.ashley.core.EntitySystem;

/**
 * In the event that the GameScreen receives input and doesn't have anything to
 * do with it, it will pass an {@link InputEvent} to this system for processing
 * within the world-update loop.
 * 
 * @author snowjak88
 *
 */
public class InputEventProcessingSystem extends EntitySystem {
	
	private final BlockingQueue<InputEvent> inputEvents = new LinkedBlockingQueue<>();
	
	public InputEventProcessingSystem() {
		
		super();
	}
	
	public void postInputEvent(InputEvent event) {
		
		inputEvents.offer(event);
	}
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		
		while (!inputEvents.isEmpty()) {
			final InputEvent event = inputEvents.poll();
			if (event == null)
				continue;
			
			System.out.println("Got an event!");
			
			EventPool.get().retire(event);
		}
	}
}
