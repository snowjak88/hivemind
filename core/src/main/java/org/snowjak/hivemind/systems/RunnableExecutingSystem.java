/**
 * 
 */
package org.snowjak.hivemind.systems;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.EntitySystem;

/**
 * If you have any updates to make to the {@link Engine} from another thread,
 * submit those updates within {@link Runnable}s to this System. Your updates
 * will be executed on the Engine-thread.
 * 
 * @author snowjak88
 *
 */
public class RunnableExecutingSystem extends EntitySystem {
	
	private static final Logger LOG = Logger.getLogger(RunnableExecutingSystem.class.getName());
	private BlockingQueue<Runnable> updates = new LinkedBlockingQueue<>();
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		
		while (!updates.isEmpty()) {
			final Runnable update = updates.poll();
			if (update == null)
				continue;
			
			update.run();
		}
	}
	
	/**
	 * Add a new {@link Runnable} to be executed on the Engine-updating thread.
	 * 
	 * @param update
	 */
	public void submit(Runnable update) {
		
		try {
			updates.put(update);
		} catch (InterruptedException e) {
			LOG.severe("Interrupted while submitting update to entity-engine updater!");
		}
	}
	
}
