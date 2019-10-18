/**
 * 
 */
package org.snowjak.hivemind.util;

import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.IRNG;
import squidpony.squidmath.RandomnessSource;

/**
 * "Extended" {@link GreasedRegion}
 * 
 * @author snowjak88
 *
 */
public class ExtGreasedRegion extends GreasedRegion {
	
	private static final long serialVersionUID = 2686731506816666394L;
	
	/**
	 * @see GreasedRegion#GreasedRegion()
	 */
	public ExtGreasedRegion() {
		
		super();
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(boolean[], int, int)
	 */
	public ExtGreasedRegion(boolean[] bits, int width, int height) {
		
		super(bits, width, height);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(boolean[])
	 */
	public ExtGreasedRegion(boolean[][] bits) {
		
		super(bits);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(char[][], char)
	 */
	public ExtGreasedRegion(char[][] map, char yes) {
		
		super(map, yes);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(Coord, int, int)
	 */
	public ExtGreasedRegion(Coord single, int width, int height) {
		
		super(single, width, height);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(double[][], double, double, int)
	 */
	public ExtGreasedRegion(double[][] map, double lowerBound, double upperBound, int scale) {
		
		super(map, lowerBound, upperBound, scale);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(double[][], double, double)
	 */
	public ExtGreasedRegion(double[][] map, double lowerBound, double upperBound) {
		
		super(map, lowerBound, upperBound);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(double[][], double)
	 */
	public ExtGreasedRegion(double[][] map, double upperBound) {
		
		super(map, upperBound);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(GreasedRegion)
	 */
	public ExtGreasedRegion(GreasedRegion other) {
		
		super(other);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(int, int, Coord...)
	 */
	public ExtGreasedRegion(int width, int height, Coord... points) {
		
		super(width, height, points);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(int, int, Iterable)
	 */
	public ExtGreasedRegion(int width, int height, Iterable<Coord> points) {
		
		super(width, height, points);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(int, int)
	 */
	public ExtGreasedRegion(int width, int height) {
		
		super(width, height);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(int[][], int, int)
	 */
	public ExtGreasedRegion(int[][] map, int lower, int upper) {
		
		super(map, lower, upper);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(int[][], int)
	 */
	public ExtGreasedRegion(int[][] map, int yes) {
		
		super(map, yes);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(IRNG, int, int)
	 */
	public ExtGreasedRegion(IRNG random, int width, int height) {
		
		super(random, width, height);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(long[], int, int, int, int)
	 */
	public ExtGreasedRegion(long[] data2, int dataWidth, int dataHeight, int width, int height) {
		
		super(data2, dataWidth, dataHeight, width, height);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(long[], int, int)
	 */
	public ExtGreasedRegion(long[] data2, int width, int height) {
		
		super(data2, width, height);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(RandomnessSource, double, int, int)
	 */
	public ExtGreasedRegion(RandomnessSource random, double fraction, int width, int height) {
		
		super(random, fraction, width, height);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(RandomnessSource, int, int)
	 */
	public ExtGreasedRegion(RandomnessSource random, int width, int height) {
		
		super(random, width, height);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(short[][], int, int)
	 */
	public ExtGreasedRegion(short[][] map, int lower, int upper) {
		
		super(map, lower, upper);
	}
	
	/**
	 * @see GreasedRegion#GreasedRegion(String[], char)
	 */
	public ExtGreasedRegion(String[] map, char yes) {
		
		super(map, yes);
	}
	
	/**
	 * Returns a copy of {@code map} where if a cell is "off" in this GreasedRegion,
	 * this keeps the value in {@code map} intact, and where a cell is "on", it
	 * instead writes the value {@code toWrite}.
	 * 
	 * @param map
	 *            a 2D array that will not be modified
	 * @param toWrite
	 *            the value to use where this GreasedRegion stores an "on" cell
	 * @return a masked copy of map
	 */
	public short[][] inverseMask(short[][] map, short toWrite) {
		
		if (map == null || map.length == 0)
			return new short[0][0];
		final int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
		final short[][] values = new short[width2][height2];
		for (int x = 0; x < width2; x++) {
			for (int y = 0; y < height2; y++) {
				values[x][y] = (data[x * getYSections() + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite : map[x][y];
			}
		}
		return values;
	}
	
	/**
	 * Returns a copy of {@code map} where if a cell is "off" in this GreasedRegion,
	 * this keeps the value in {@code map} intact, and where a cell is "on", it
	 * instead writes the corresponding value in {@code toWrite}.
	 * 
	 * @param map
	 *            a 2D array that will not be modified
	 * @param toWrite
	 *            the map to use where this GreasedRegion stores an "on" cell
	 * @return a masked copy of map
	 */
	public char[][] inverseMask(char[][] map, char[][] toWrite) {
		
		if (map == null || map.length == 0)
			return new char[0][0];
		final int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
		final char[][] values = new char[width2][height2];
		for (int x = 0; x < width2; x++) {
			for (int y = 0; y < height2; y++) {
				values[x][y] = (data[x * getYSections() + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite[x][y]
						: map[x][y];
			}
		}
		return values;
	}
	
	/**
	 * Returns a copy of {@code map} where if a cell is "off" in this GreasedRegion,
	 * this keeps the value in {@code map} intact, and where a cell is "on", it
	 * instead writes the corresponding value in {@code toWrite}.
	 * 
	 * @param map
	 *            a 2D array that will not be modified
	 * @param toWrite
	 *            the map to use where this GreasedRegion stores an "on" cell
	 * @return a masked copy of map
	 */
	public short[][] inverseMask(short[][] map, short[][] toWrite) {
		
		if (map == null || map.length == 0)
			return new short[0][0];
		final int width2 = Math.min(width, map.length), height2 = Math.min(height, map[0].length);
		final short[][] values = new short[width2][height2];
		for (int x = 0; x < width2; x++) {
			for (int y = 0; y < height2; y++) {
				values[x][y] = (data[x * getYSections() + (y >> 6)] & (1L << (y & 63))) != 0 ? toWrite[x][y]
						: map[x][y];
			}
		}
		return values;
	}
	
	/**
	 * Required because {@code ySections} is a private field.
	 * 
	 * @return
	 */
	private int getYSections() {
		
		return (height + 63) >> 6;
	}
}
