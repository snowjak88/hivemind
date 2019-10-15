package org.snowjak.hivemind.concurrent;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Represents a process which executes continuously once initiated.
 * Implementations must ensure that they check {@link #isStopped()} regularly,
 * and halt their execution accordingly.
 * 
 * @author snowjak88
 *
 */
public abstract class ContinuousProcess {
	
	private static final Collection<ContinuousProcess> processes = Collections.synchronizedList(new LinkedList<>());
	
	private static void register(ContinuousProcess process) {
		
		processes.add(process);
	}
	
	private static void unregister(ContinuousProcess process) {
		
		processes.remove(process);
	}
	
	/**
	 * @return all created and active {@link ContinuousProcess} instances
	 */
	public static Collection<ContinuousProcess> getActiveProcesses() {
		
		return processes;
	}
	
	private boolean isStopped = false;
	
	/**
	 * This process may receive a halt-and-shutdown message one of two ways:
	 * <ul>
	 * <li>Explicitly, via {@link #stop()}</li>
	 * <li>Implicitly, via {@link Thread#interrupted()}</li>
	 * </ul>
	 * 
	 * @return whether this process has received a halt-and-shutdown message,
	 *         whether implicitly or explicitly
	 */
	public boolean isStopped() {
		
		if (Thread.interrupted())
			stop();
		return isStopped;
	}
	
	/**
	 * You may call this method to explicitly order a shutdown.
	 * <p>
	 * Also removes this process from the
	 * {@link ContinuousProcess#getActiveProcesses() list of active processes}.
	 * </p>
	 */
	public void stop() {
		
		ContinuousProcess.unregister(this);
		isStopped = true;
	}
	
	/**
	 * Your main thread should call this to initiate execution.
	 * <p>
	 * This also adds this process to the
	 * {@link ContinuousProcess#getActiveProcesses() list of active processes}.
	 * </p>
	 */
	public void start() {
		
		ContinuousProcess.register(this);
		
		starting();
		
		run();
		
		stopping();
	}
	
	/**
	 * This method is called (on the process-thread) when this ContinuousProcess is
	 * first created.
	 * <p>
	 * Implementations should use this to initialize any required resources.
	 * </p>
	 */
	public abstract void starting();
	
	/**
	 * Your implementation should implement this, probably as a loop.
	 * <p>
	 * You <em>must</em> ensure that you regularly call {@link #isStopped()}, to
	 * detect halt-and-shutdown requests from the main-thread.
	 * </p>
	 */
	public abstract void run();
	
	/**
	 * This method is called (on the process-thread) after this ContinuousProcess
	 * has received a shutdown signal.
	 * <p>
	 * Implementations should use this to dispose of any resources initialized in
	 * {@link #starting()}.
	 * </p>
	 */
	public abstract void stopping();
}
