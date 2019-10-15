/**
 * 
 */
package org.snowjak.hivemind.concurrent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Represents a process that should synchronize with the current frame-rate.
 * Implementations will implement {@link #processFrame(float)}, which will
 * receive the current-frame's "seconds-since-last-frame".
 * <p>
 * Note that this class provides synchronization only <strong>once</strong> per
 * frame. This process will <em>wait</em> to receive the current frame-time, and
 * the main thread will <em>wait</em> until this process has picked-up the
 * current frame-time.
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class PerFrameProcess {
	
	private static final Collection<PerFrameProcess> processes = Collections.synchronizedList(new LinkedList<>());
	
	private static void register(PerFrameProcess process) {
		
		processes.add(process);
	}
	
	private static void unregister(PerFrameProcess process) {
		
		processes.remove(process);
	}
	
	/**
	 * @return all created and active {@link PerFrameProcess} instances
	 */
	public static Collection<PerFrameProcess> getActiveProcesses() {
		
		return processes;
	}
	
	private final BlockingQueue<Float> updateQueue = new ArrayBlockingQueue<>(1);
	
	private boolean isStopped = false;
	
	public Future<?> start() {
		
		final Future<?> f = Executor.get().submit(() -> {
			
			starting();
			
			PerFrameProcess.register(this);
			
			try {
				
				while (!Thread.interrupted() && !isStopped) {
					final Float delta = updateQueue.poll(1, TimeUnit.SECONDS);
					if (delta != null)
						processFrame(delta);
				}
				
			} catch (InterruptedException e) {
				
			} finally {
				
				PerFrameProcess.unregister(this);
				
				stopping();
			}
			
		});
		
		return f;
	}
	
	/**
	 * Your main thread should call this every frame. Effectively, this allows this
	 * PerFrameProcess to <em>start</em> processing the current frame.
	 * <p>
	 * This method will <strong>block</strong> until the PerFrameProcess finishes
	 * processing the last frame, at which point it will be available to take the
	 * next update.
	 * </p>
	 * 
	 * @param delta
	 */
	public void update(float delta) {
		
		try {
			updateQueue.put(delta);
		} catch (InterruptedException e) {
			isStopped = true;
		}
	}
	
	/**
	 * Signal this PerFrameProcess to stop as soon as the current frame finishes.
	 */
	public void stop() {
		
		isStopped = true;
	}
	
	/**
	 * This method is called (on the process-thread) when this PerFrameProcess is
	 * first created.
	 * <p>
	 * Implementations should use this to initialize any required resources.
	 * </p>
	 */
	public abstract void starting();
	
	/**
	 * This method is called (on the process-thread) once per frame with the current
	 * "seconds-since" delta.
	 * 
	 * @param delta
	 */
	public abstract void processFrame(float delta);
	
	/**
	 * This method is called (on the process-thread) after this PerFrameProcess has
	 * received a shutdown signal.
	 * <p>
	 * Implementations should use this to dispose of any resources initialized in
	 * {@link #starting()}.
	 * </p>
	 */
	public abstract void stopping();
}
