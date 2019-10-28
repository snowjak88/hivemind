/**
 * 
 */
package org.snowjak.hivemind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.snowjak.hivemind.config.Config;

import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.IRNG;

/**
 * Provides access to a singleton {@link IRNG} instance.
 * 
 * @author snowjak88
 *
 */
public class RNG implements IRNG {
	
	private static final long serialVersionUID = 8317838413092083047L;
	
	public static final String PREFERENCE_SEED = "rng.seed";
	{
		Config.get().register(PREFERENCE_SEED, "RNG seed", "hivemind", true, true);
	}
	
	private static RNG __INSTANCE = null;
	
	public static RNG get() {
		
		if (__INSTANCE == null)
			synchronized (RNG.class) {
				if (__INSTANCE == null)
					__INSTANCE = new RNG();
			}
		return __INSTANCE;
	}
	
	private final GWTRNG rng;
	
	private RNG() {
		
		rng = new GWTRNG(getSeed());
	}
	
	public String getSeed() {
		
		return Config.get().get(PREFERENCE_SEED);
	}
	
	public void setSeed(String seed) {
		
		Config.get().set(PREFERENCE_SEED, seed);
	}
	
	/**
	 * @param bound
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#nextLong(long)
	 */
	public long nextLong(long bound) {
		
		return rng.nextLong(bound);
	}
	
	/**
	 * @param bits
	 * @return
	 * @see squidpony.squidmath.GWTRNG#next(int)
	 */
	public final int next(int bits) {
		
		return rng.next(bits);
	}
	
	/**
	 * @param outer
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#nextDouble(double)
	 */
	public double nextDouble(double outer) {
		
		return rng.nextDouble(outer);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#nextInt()
	 */
	public final int nextInt() {
		
		return rng.nextInt();
	}
	
	/**
	 * @param bound
	 * @return
	 * @see squidpony.squidmath.GWTRNG#nextInt(int)
	 */
	public final int nextInt(int bound) {
		
		return rng.nextInt(bound);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#nextLong()
	 */
	public final long nextLong() {
		
		return rng.nextLong();
	}
	
	/**
	 * @param outer
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#nextFloat(float)
	 */
	public float nextFloat(float outer) {
		
		return rng.nextFloat(outer);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#nextBoolean()
	 */
	public final boolean nextBoolean() {
		
		return rng.nextBoolean();
	}
	
	/**
	 * @param bound
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#nextSignedLong(long)
	 */
	public long nextSignedLong(long bound) {
		
		return rng.nextSignedLong(bound);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#nextDouble()
	 */
	public final double nextDouble() {
		
		return rng.nextDouble();
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#nextFloat()
	 */
	public final float nextFloat() {
		
		return rng.nextFloat();
	}
	
	/**
	 * @param bound
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#nextSignedInt(int)
	 */
	public int nextSignedInt(int bound) {
		
		return rng.nextSignedInt(bound);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#copy()
	 */
	public GWTRNG copy() {
		
		return rng.copy();
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#toSerializable()
	 */
	public Serializable toSerializable() {
		
		return rng.toSerializable();
	}
	
	/**
	 * @param min
	 * @param max
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#between(int, int)
	 */
	public int between(int min, int max) {
		
		return rng.between(min, max);
	}
	
	/**
	 * @param seed
	 * @see squidpony.squidmath.GWTRNG#setSeed(int)
	 */
	public void setSeed(int seed) {
		
		rng.setSeed(seed);
	}
	
	/**
	 * @param min
	 * @param max
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#between(long, long)
	 */
	public long between(long min, long max) {
		
		return rng.between(min, max);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#getStateA()
	 */
	public int getStateA() {
		
		return rng.getStateA();
	}
	
	/**
	 * @param stateA
	 * @see squidpony.squidmath.GWTRNG#setStateA(int)
	 */
	public void setStateA(int stateA) {
		
		rng.setStateA(stateA);
	}
	
	/**
	 * @param min
	 * @param max
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#between(double, double)
	 */
	public double between(double min, double max) {
		
		return rng.between(min, max);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#getStateB()
	 */
	public int getStateB() {
		
		return rng.getStateB();
	}
	
	/**
	 * @param stateB
	 * @see squidpony.squidmath.GWTRNG#setStateB(int)
	 */
	public void setStateB(int stateB) {
		
		rng.setStateB(stateB);
	}
	
	/**
	 * @param <T>
	 * @param array
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#getRandomElement(java.lang.Object[])
	 */
	public <T> T getRandomElement(T[] array) {
		
		return rng.getRandomElement(array);
	}
	
	/**
	 * @param <T>
	 * @param list
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#getRandomElement(java.util.List)
	 */
	public <T> T getRandomElement(List<T> list) {
		
		return rng.getRandomElement(list);
	}
	
	/**
	 * @param stateA
	 * @param stateB
	 * @see squidpony.squidmath.GWTRNG#setState(int, int)
	 */
	public void setState(int stateA, int stateB) {
		
		rng.setState(stateA, stateB);
	}
	
	/**
	 * @param <T>
	 * @param coll
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#getRandomElement(java.util.Collection)
	 */
	public <T> T getRandomElement(Collection<T> coll) {
		
		return rng.getRandomElement(coll);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#getState()
	 */
	public long getState() {
		
		return rng.getState();
	}
	
	/**
	 * @param state
	 * @see squidpony.squidmath.GWTRNG#setState(long)
	 */
	public void setState(long state) {
		
		rng.setState(state);
	}
	
	/**
	 * @param o
	 * @return
	 * @see squidpony.squidmath.GWTRNG#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		
		return rng.equals(o);
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#hashCode()
	 */
	public int hashCode() {
		
		return rng.hashCode();
	}
	
	/**
	 * @return
	 * @see squidpony.squidmath.GWTRNG#toString()
	 */
	public String toString() {
		
		return rng.toString();
	}
	
	/**
	 * @param <T>
	 * @param elements
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#shuffle(java.lang.Object[])
	 */
	public <T> T[] shuffle(T[] elements) {
		
		return rng.shuffle(elements);
	}
	
	/**
	 * @param <T>
	 * @param elements
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#shuffleInPlace(java.lang.Object[])
	 */
	public <T> T[] shuffleInPlace(T[] elements) {
		
		return rng.shuffleInPlace(elements);
	}
	
	/**
	 * @param <T>
	 * @param elements
	 * @param dest
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#shuffle(java.lang.Object[],
	 *      java.lang.Object[])
	 */
	public <T> T[] shuffle(T[] elements, T[] dest) {
		
		return rng.shuffle(elements, dest);
	}
	
	/**
	 * @param <T>
	 * @param elements
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#shuffle(java.util.Collection)
	 */
	public <T> ArrayList<T> shuffle(Collection<T> elements) {
		
		return rng.shuffle(elements);
	}
	
	/**
	 * @param <T>
	 * @param elements
	 * @param buf
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#shuffle(java.util.Collection,
	 *      java.util.ArrayList)
	 */
	public <T> ArrayList<T> shuffle(Collection<T> elements, ArrayList<T> buf) {
		
		return rng.shuffle(elements, buf);
	}
	
	/**
	 * @param <T>
	 * @param elements
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#shuffleInPlace(java.util.List)
	 */
	public <T> List<T> shuffleInPlace(List<T> elements) {
		
		return rng.shuffleInPlace(elements);
	}
	
	/**
	 * @param length
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#randomOrdering(int)
	 */
	public int[] randomOrdering(int length) {
		
		return rng.randomOrdering(length);
	}
	
	/**
	 * @param length
	 * @param dest
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#randomOrdering(int, int[])
	 */
	public int[] randomOrdering(int length, int[] dest) {
		
		return rng.randomOrdering(length, dest);
	}
	
	/**
	 * @param <T>
	 * @param data
	 * @param output
	 * @return
	 * @see squidpony.squidmath.AbstractRNG#randomPortion(java.lang.Object[],
	 *      java.lang.Object[])
	 */
	public <T> T[] randomPortion(T[] data, T[] output) {
		
		return rng.randomPortion(data, output);
	}
	
}
