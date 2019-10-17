/**
 * 
 */
package org.snowjak.hivemind.util;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.badlogic.gdx.utils.ObjectMap;
import com.google.common.base.Strings;

/**
 * Amasses profiling stats -- time spent doing some task or other.
 * <p>
 * Fundamentally, a Profiler associates "labels" (i.e., arbitrary Strings) with
 * {@link Duration}s. To profile a particular section of code, you would write,
 * e.g.
 * 
 * <pre>
 * ...
 * 
 * final ProfilerTimer myWorkTimer = Profiler.get().start("doing some work now");
 * 
 *    [ do some work now ]
 * 
 * myWorkTimer.stop();
 * ...
 * 
 * //Finally, log the Profiler's report to standard output.
 * //
 * Profiler.get().report();
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class Profiler {
	
	private static Profiler __INSTANCE = null;
	
	private final ObjectMap<String, Duration> record = new ObjectMap<>();
	
	private final BlockingQueue<ProfilerTimer> timerCache = new LinkedBlockingQueue<>();
	
	/**
	 * Get the singleton Profiler instance.
	 * 
	 * @return
	 */
	public static Profiler get() {
		
		if (__INSTANCE == null)
			synchronized (Profiler.class) {
				if (__INSTANCE == null)
					__INSTANCE = new Profiler();
			}
		
		return __INSTANCE;
	}
	
	private Profiler() {
		
	}
	
	/**
	 * Start a new timer associated with the given label.
	 * 
	 * @param label
	 * @return the newly-started timer
	 */
	public ProfilerTimer start(String label) {
		
		ProfilerTimer t = timerCache.poll();
		if (t == null)
			t = new ProfilerTimer(this);
		
		t.setup(label, Instant.now());
		return t;
	}
	
	private void stop(ProfilerTimer timer) {
		
		synchronized (this) {
			final Instant end = Instant.now();
			
			final Duration existingRecord = this.record.get(timer.label, Duration.ZERO);
			this.record.put(timer.label, existingRecord.plus(Duration.between(timer.start, end)));
			
			timerCache.add(timer);
		}
	}
	
	/**
	 * Log all recorded labels and their durations to standard output.
	 */
	public void report() {
		
		synchronized (this) {
			
			int maxLabelLength = Integer.MIN_VALUE;
			for (String label : this.record.keys())
				maxLabelLength = Math.max(maxLabelLength, label.length());
			
			System.out.println();
			System.out.println("-=-=-=-=-=-=-=-=- PROFILING RESULTS -=-=-=-=-=-=-=-=-");
			System.out.println();
			
			final List<String> labels = new LinkedList<>();
			this.record.keys().forEach(labels::add);
			labels.sort(String::compareTo);
			
			if (record.isEmpty())
				System.out.println("    (no results)");
			else
				for (String label : labels) {
					System.out.print("   [");
					System.out.print(label);
					System.out.print("]");
					System.out.print(Strings.repeat(" ", maxLabelLength - label.length() + 3));
					System.out.println(this.record.get(label).toString());
				}
			System.out.println();
			System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
		}
	}
	
	/**
	 * An individual timer associated with the Profiler. Unique to the label/thread
	 * that requests it.
	 * <p>
	 * Note that, because these timer instances are cached, you must <em>not</em>
	 * attempt to refer to this timer after calling {@link #stop()}!
	 * </p>
	 * 
	 * @author snowjak88
	 *
	 */
	public static class ProfilerTimer {
		
		private final Profiler p;
		private String label;
		private Instant start;
		
		private ProfilerTimer(Profiler p) {
			
			this.p = p;
		}
		
		private void setup(String label, Instant start) {
			
			this.label = label;
			this.start = start;
		}
		
		/**
		 * Stop this timer, relaying its results back to the {@link Profiler}.
		 * <p>
		 * Note that this method will <em>invalidate</em> this timer, so you should not
		 * refer to it any longer!
		 * </p>
		 */
		public void stop() {
			
			p.stop(this);
		}
	}
}
