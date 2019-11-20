/**
 * 
 */
package org.snowjak.hivemind.util;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * Provides various helper-functions related to arrays.
 * 
 * @author snowjak88
 *
 */
public class ArrayUtil {
	
	/**
	 * Copy the given array.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 * 
	 * @param toCopy
	 * @return
	 */
	public static char[][] copy(char[][] toCopy) {
		
		final char[][] result = new char[toCopy.length][];
		
		for (int i = 0; i < toCopy.length; i++) {
			result[i] = new char[toCopy[i].length];
			System.arraycopy(toCopy[i], 0, result[i], 0, toCopy[i].length);
		}
		
		return result;
	}
	
	/**
	 * Copy the given array.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 * 
	 * @param toCopy
	 * @return
	 */
	public static short[][] copy(short[][] toCopy) {
		
		final short[][] result = new short[toCopy.length][];
		
		for (int i = 0; i < toCopy.length; i++) {
			result[i] = new short[toCopy[i].length];
			System.arraycopy(toCopy[i], 0, result[i], 0, toCopy[i].length);
		}
		
		return result;
	}
	
	/**
	 * Copy the given array.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 * 
	 * @param toCopy
	 * @return
	 */
	public static int[][] copy(int[][] toCopy) {
		
		final int[][] result = new int[toCopy.length][];
		
		for (int i = 0; i < toCopy.length; i++) {
			result[i] = new int[toCopy[i].length];
			System.arraycopy(toCopy[i], 0, result[i], 0, toCopy[i].length);
		}
		
		return result;
	}
	
	/**
	 * Copy the given array.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 * 
	 * @param toCopy
	 * @return
	 */
	public static float[][] copy(float[][] toCopy) {
		
		final float[][] result = new float[toCopy.length][];
		
		for (int i = 0; i < toCopy.length; i++) {
			result[i] = new float[toCopy[i].length];
			System.arraycopy(toCopy[i], 0, result[i], 0, toCopy[i].length);
		}
		
		return result;
	}
	
	/**
	 * Copy the given array.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 * 
	 * @param toCopy
	 * @return
	 */
	public static double[][] copy(double[][] toCopy) {
		
		final double[][] result = new double[toCopy.length][];
		
		for (int i = 0; i < toCopy.length; i++) {
			result[i] = new double[toCopy[i].length];
			System.arraycopy(toCopy[i], 0, result[i], 0, toCopy[i].length);
		}
		
		return result;
	}
	
	/**
	 * Copy the given array.
	 * <p>
	 * Note that this performs a "shallow" copy -- i.e., object-references are
	 * copied instead of new objects being created.
	 * </p>
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 * 
	 * @param toCopy
	 * @param makeOuterArray
	 *            function to construct an outer array of the given length -- e.g.,
	 *            {@code new Object[len][]}
	 * @param make1dArray
	 *            function to construct a 1D array of the given length -- e.g.,
	 *            {@code new Object[len]}
	 * @return
	 * @see #copy(Object[][], IntFunction, IntFunction, Function)
	 */
	public static <T> T[][] copy(T[][] toCopy, IntFunction<T[][]> makeOuterArray, IntFunction<T[]> make1dArray) {
		
		final T[][] result = makeOuterArray.apply(toCopy.length);
		
		for (int i = 0; i < toCopy.length; i++) {
			result[i] = make1dArray.apply(toCopy[i].length);
			System.arraycopy(toCopy[i], 0, result[i], 0, toCopy[i].length);
		}
		
		return result;
	}
	
	/**
	 * Copy the given array.
	 * <p>
	 * Note that this performs a "deep" copy -- i.e., new objects are created
	 * instead of object-references being copied.
	 * </p>
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 * 
	 * @param toCopy
	 * @param makeOuterArray
	 *            function to construct an outer array of the given length -- e.g.,
	 *            {@code new Object[len][]}
	 * @param make1dArray
	 *            function to construct a 1D array of the given length -- e.g.,
	 *            {@code new Object[len]}
	 * @param makeCopy
	 *            function to construct a copy of a given object
	 * @return
	 * @see #copy(Object[][], IntFunction, IntFunction)
	 */
	public static <T> T[][] copy(T[][] toCopy, IntFunction<T[][]> makeOuterArray, IntFunction<T[]> make1dArray,
			Function<T, T> makeCopy) {
		
		final T[][] result = makeOuterArray.apply(toCopy.length);
		
		for (int i = 0; i < toCopy.length; i++) {
			result[i] = make1dArray.apply(toCopy[i].length);
			for (int j = 0; j < toCopy[i].length; j++)
				result[i][j] = makeCopy.apply(toCopy[i][j]);
		}
		
		return result;
	}
	
	/**
	 * Fills this array with the given {@code value}.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 */
	public static void fill(char[][] array, char value) {
		
		for (int i = 0; i < array.length; i++)
			Arrays.fill(array[i], value);
	}
	
	/**
	 * Fills this array with the given {@code value}.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 */
	public static void fill(short[][] array, short value) {
		
		for (int i = 0; i < array.length; i++)
			Arrays.fill(array[i], value);
	}
	
	/**
	 * Fills this array with the given {@code value}.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 */
	public static void fill(int[][] array, int value) {
		
		for (int i = 0; i < array.length; i++)
			Arrays.fill(array[i], value);
	}
	
	/**
	 * Fills this array with the given {@code value}.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 */
	public static void fill(float[][] array, float value) {
		
		for (int i = 0; i < array.length; i++)
			Arrays.fill(array[i], value);
	}
	
	/**
	 * Fills this array with the given {@code value}.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 */
	public static void fill(double[][] array, double value) {
		
		for (int i = 0; i < array.length; i++)
			Arrays.fill(array[i], value);
	}
	
	/**
	 * Fills this array with the given {@code value}.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 */
	public static <T> void fill(T[][] array, T value) {
		
		for (int i = 0; i < array.length; i++)
			Arrays.fill(array[i], value);
	}
	
	/**
	 * Fills this array with the given {@code value}.
	 * <p>
	 * Note that {@code toCopy} may safely be "jagged" -- i.e.,
	 * {@code toCopy[0].length != toCopy[1].length}
	 * </p>
	 */
	public static <T> void fill(T[][] array, Supplier<T> supplier) {
		
		for (int i = 0; i < array.length; i++)
			Arrays.fill(array[i], supplier.get());
	}
	
	/**
	 * Fills the given {@code array} with values taken from {@code values}. You
	 * might also describe this as "copying in place".
	 * 
	 * @param array
	 * @param values
	 * @throws IllegalArgumentException
	 *             if the two arrays are not of the same size
	 */
	public static void fill(char[][] array, char[][] values) {
		
		if (array.length != values.length)
			throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
		
		for (int i = 0; i < array.length; i++) {
			if (array[i].length != values[i].length)
				throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
			for (int j = 0; j < array[i].length; j++)
				array[i][j] = values[i][j];
		}
	}
	
	/**
	 * Fills the given {@code array} with values taken from {@code values}. You
	 * might also describe this as "copying in place".
	 * 
	 * @param array
	 * @param values
	 * @throws IllegalArgumentException
	 *             if the two arrays are not of the same size
	 */
	public static void fill(short[][] array, short[][] values) {
		
		if (array.length != values.length)
			throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
		
		for (int i = 0; i < array.length; i++) {
			if (array[i].length != values[i].length)
				throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
			for (int j = 0; j < array[i].length; j++)
				array[i][j] = values[i][j];
		}
	}
	
	/**
	 * Fills the given {@code array} with values taken from {@code values}. You
	 * might also describe this as "copying in place".
	 * 
	 * @param array
	 * @param values
	 * @throws IllegalArgumentException
	 *             if the two arrays are not of the same size
	 */
	public static void fill(int[][] array, int[][] values) {
		
		if (array.length != values.length)
			throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
		
		for (int i = 0; i < array.length; i++) {
			if (array[i].length != values[i].length)
				throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
			for (int j = 0; j < array[i].length; j++)
				array[i][j] = values[i][j];
		}
	}
	
	/**
	 * Fills the given {@code array} with values taken from {@code values}. You
	 * might also describe this as "copying in place".
	 * 
	 * @param array
	 * @param values
	 * @throws IllegalArgumentException
	 *             if the two arrays are not of the same size
	 */
	public static void fill(float[][] array, float[][] values) {
		
		if (array.length != values.length)
			throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
		
		for (int i = 0; i < array.length; i++) {
			if (array[i].length != values[i].length)
				throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
			for (int j = 0; j < array[i].length; j++)
				array[i][j] = values[i][j];
		}
	}
	
	/**
	 * Fills the given {@code array} with values taken from {@code values}. You
	 * might also describe this as "copying in place".
	 * 
	 * @param array
	 * @param values
	 * @throws IllegalArgumentException
	 *             if the two arrays are not of the same size
	 */
	public static void fill(double[][] array, double[][] values) {
		
		if (array.length != values.length)
			throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
		
		for (int i = 0; i < array.length; i++) {
			if (array[i].length != values[i].length)
				throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
			for (int j = 0; j < array[i].length; j++)
				array[i][j] = values[i][j];
		}
	}
	
	/**
	 * Fills the given {@code array} with values taken from {@code values}. You
	 * might also describe this as "copying in place".
	 * <p>
	 * Note that this function performs only a "shallow" copy, where
	 * object-references are copied (instead of full-blown objects being copied).
	 * </p>
	 * 
	 * @param array
	 * @param values
	 * @throws IllegalArgumentException
	 *             if the two arrays are not of the same size
	 */
	public static <T> void fill(T[][] array, T[][] values) {
		
		if (array.length != values.length)
			throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
		
		for (int i = 0; i < array.length; i++) {
			if (array[i].length != values[i].length)
				throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
			for (int j = 0; j < array[i].length; j++)
				array[i][j] = values[i][j];
		}
	}
	
	/**
	 * Fills the given {@code array} with values taken from {@code values}. You
	 * might also describe this as "copying in place".
	 * <p>
	 * This function performs a "deep copy", using {@code copier} to make a copy of
	 * each object in {@code values}.
	 * </p>
	 * 
	 * @param array
	 * @param values
	 * @param copier
	 * @throws IllegalArgumentException
	 *             if the two arrays are not of the same size
	 */
	public static <T> void fill(T[][] array, T[][] values, Function<T, T> copier) {
		
		if (array.length != values.length)
			throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
		
		for (int i = 0; i < array.length; i++) {
			if (array[i].length != values[i].length)
				throw new IllegalArgumentException("Cannot fill-in-place: given arrays are of different sizes!");
			for (int j = 0; j < array[i].length; j++)
				if (values[i][j] == null)
					array[i][j] = null;
				else
					array[i][j] = copier.apply(values[i][j]);
		}
	}
	
	/**
	 * Adds the values in {@code addend} to {@code value}, storing the resulting
	 * sums in {@code value}.
	 * 
	 * @param value
	 * @param addend
	 * @throws IllegalArgumentException
	 *             if the two arrays' sizes do not match
	 */
	public static void addInPlace(double[][] value, double[][] addend) {
		
		if (value.length != addend.length)
			throw new IllegalArgumentException("Cannot addInPlace two arrays not of the same size!");
		
		for (int i = 0; i < value.length; i++) {
			if (value[i].length != addend[i].length)
				throw new IllegalArgumentException("Cannot addInPlace two arrays not of the same size!");
			for (int j = 0; j < value[i].length; j++)
				value[i][j] += addend[i][j];
		}
	}
	
	/**
	 * Construct a new array by appending the given {@code values} to the given
	 * {@code array}.
	 * 
	 * @param lenFunction
	 *            a function which constructs a type-specific array of the given
	 *            length
	 * @param array
	 * @param values
	 * @return
	 */
	@SafeVarargs
	public static <T> T[] concat(IntFunction<T[]> lenFunction, T[] array, T... values) {
		
		final T[] result = lenFunction.apply(array.length + values.length);
		for (int i = 0; i < array.length; i++)
			result[i] = array[i];
		for (int i = 0; i < values.length; i++)
			result[i + array.length] = values[i];
		return result;
	}
}
