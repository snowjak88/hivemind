/**
 * 
 */
package org.snowjak.hivemind.util.loaders;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.ShortBuffer;
import java.util.Base64;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.GreasedRegion;

/**
 * @author snowjak88
 *
 */
public interface Loader<T> extends JsonSerializer<T>, JsonDeserializer<T> {
	
	/**
	 * Convert the given {@code char[][]} to a {@link Base64}-encoded String.
	 * 
	 * @param values
	 * @return
	 */
	public default String toBase64(char[][] values) {
		
		int length = 0;
		for (int i = 0; i < values.length; i++)
			length += values[i].length;
		
		final ByteBuffer buffer = ByteBuffer.allocate(length * Character.BYTES);
		final CharBuffer valueBuffer = buffer.asCharBuffer();
		for (int i = 0; i < values.length; i++)
			valueBuffer.put(values[i]);
		
		buffer.rewind();
		
		return Base64.getEncoder().encodeToString(buffer.array());
	}
	
	/**
	 * Convert the given {@code short[]} to a {@link Base64}-encoded String.
	 * 
	 * @param values
	 * @return
	 */
	public default String toBase64(short[] values) {
		
		final ByteBuffer buffer = ByteBuffer.allocate(values.length * Short.BYTES);
		final ShortBuffer valueBuffer = buffer.asShortBuffer();
		
		valueBuffer.put(values);
		
		buffer.rewind();
		
		return Base64.getEncoder().encodeToString(buffer.array());
	}
	
	/**
	 * Convert the given {@code short[][]} to a {@link Base64}-encoded String.
	 * 
	 * @param values
	 * @return
	 */
	public default String toBase64(short[][] values) {
		
		int length = 0;
		for (int i = 0; i < values.length; i++)
			length += values[i].length;
		
		final ByteBuffer buffer = ByteBuffer.allocate(length * Character.BYTES);
		final ShortBuffer valueBuffer = buffer.asShortBuffer();
		for (int i = 0; i < values.length; i++)
			valueBuffer.put(values[i]);
		
		buffer.rewind();
		
		return Base64.getEncoder().encodeToString(buffer.array());
	}
	
	/**
	 * Convert the given {@link GreasedRegion} to a {@link Base64}-encoded String.
	 * 
	 * @param values
	 * @return
	 */
	public default String toBase64(GreasedRegion region) {
		
		return toBase64(CoordPacker.packSeveral(region));
	}
	
	/**
	 * Convert the given {@link Base64}-encoded String to a (non-jagged)
	 * {@code char[][]} with the given dimensions.
	 * 
	 * @param base64
	 * @param width
	 * @param height
	 * @return
	 */
	public default char[][] toCharArray(String base64, int width, int height) {
		
		final char[][] result = new char[width][height];
		final CharBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(base64)).asCharBuffer();
		
		buffer.rewind();
		
		for (int i = 0; i < width; i++)
			buffer.get(result[i], 0, height);
		
		return result;
	}
	
	/**
	 * Convert the given {@link Base64}-encoded String to a {@code short[]}.
	 * 
	 * @param base64
	 * @return
	 */
	public default short[] toShortArray(String base64) {
		
		final ShortBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(base64)).asShortBuffer();
		
		buffer.rewind();
		
		final short[] result = new short[buffer.limit()];
		for (int i = 0; i < result.length; i++)
			result[i] = buffer.get();
		return result;
	}
	
	/**
	 * Convert the given {@link Base64}-encoded String to a (non-jagged)
	 * {@code short[][]} with the given dimensions.
	 * 
	 * @param base64
	 * @param width
	 * @param height
	 * @return
	 */
	public default short[][] toShort2DArray(String base64, int width, int height) {
		
		final short[][] result = new short[width][height];
		final ShortBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(base64)).asShortBuffer();
		
		buffer.rewind();
		
		for (int i = 0; i < width; i++)
			buffer.get(result[i], 0, height);
		
		return result;
	}
	
	/**
	 * Convert the given {@link Base64}-encoded String to a GreasedRegion with the
	 * given dimensions.
	 * 
	 * @param base64
	 * @param width
	 * @param height
	 * @return
	 */
	public default GreasedRegion toGreasedRegion(String base64, int width, int height) {
		
		return CoordPacker.unpackGreasedRegion(toShortArray(base64), width, height);
	}
}