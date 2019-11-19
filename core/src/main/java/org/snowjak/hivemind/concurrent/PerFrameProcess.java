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
import java.util.logging.Logger;

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
	
	private static final Logger LOG = Logger.getLogger(PerFrameProcess.class.getName());
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
	private CrashHandler crashHandler = null;
	private Status status = Status.FRESH;
	
	/**
	 * Start this {@link PerFrameProcess}. This process will immediately call its
	 * {@link #starting()} method and then wait for the first "delta"-time.
	 * 
	 * @return the {@link Future} associated with this process, which you could use
	 *         to cancel it
	 * @throw {@link IllegalStateException} if this PerFrameProcess has already been
	 *        started
	 */
	public Future<?> start() {
		
		synchronized (this) {
			if (status == Status.STARTED)
				throw new IllegalStateException("Cannot start this PerFrameProcess -- already started");
			if (status == Status.RUNNING)
				throw new IllegalStateException("Cannot start this PerFrameProcess -- already running");
			if (status == Status.KILLED)
				throw new IllegalStateException("Cannot start this PerFrameProcess -- is killed");
			
			status = Status.STARTED;
			
			final Future<?> result = Executor.get().submit(() -> {
				
				starting();
				
				PerFrameProcess.register(this);
				
				try {
					
					while (!Thread.interrupted() && status != Status.STOPPED && status != Status.KILLED) {
						final Float delta = updateQueue.poll(1, TimeUnit.SECONDS);
						if (delta != null)
							processFrame(delta);
					}
					
				} catch (Throwable t) {
					LOG.severe("Unexpected exception! " + t.getClass().getSimpleName() + ": " + t.getMessage());
					if (crashHandler != null)
						crashHandler.handle(t);
					
				} finally {
					
					PerFrameProcess.unregister(this);
					
					stopping();
				}
				
			});
			
			status = Status.RUNNING;
			
			return result;
		}
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
	 * @throws IllegalStateException
	 *             if this method is called before a call to {@link #start()}
	 */
	public void update(float delta) {
		
		if (status != Status.RUNNING) {
			LOG.warning("Cannot update this PerFrameProcess because it is not running!");
			return;
		}
		try {
			updateQueue.put(delta);
		} catch (InterruptedException e) {
			status = Status.STOPPED;
		}
	}
	
	/**
	 * Signal this PerFrameProcess to stop as soon as the current frame finishes.
	 */
	public void stop() {
		
		status = Status.STOPPED;
	}
	
	public void kill() {
		
		status = Status.KILLED;
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
	
	/**
	 * If this PerFrameProcess should crash (i.e., encounter an un-handled
	 * Throwable), then the given {@link CrashHandler} will be executed. Your parent
	 * code might use this to notify itself when its PerFrameProcess has died.
	 * 
	 * @param crashHandler
	 *            {@code null} if no such "on-crash" process should run
	 */
	public void setOnProcessCrash(CrashHandler crashHandler) {
		
		this.crashHandler = crashHandler;
	}
	
	public enum Status {
		FRESH,
		STARTED,
		RUNNING,
		STOPPED,
		KILLED
	}
	
	@FunctionalInterface
	public interface CrashHandler {
		
		public void handle(Throwable t);
	}
}
