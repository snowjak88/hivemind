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
		
		final boolean[][] decoded = region.decode();
		final ByteBuffer buffer = ByteBuffer.allocate((int) Math.ceil((float) region.height / 8f) * region.width);
		
		for (int i = 0; i < decoded.length; i++) {
			byte scratch = 0;
			for (int j = 0; j < decoded[i].length; j++) {
				if (scratch > 0 && scratch % 8 == 7) {
					buffer.put(scratch);
					scratch = 0;
				} else {
					scratch += (decoded[i][j]) ? (byte) 1 : (byte) 0;
					scratch = (byte) (scratch << 1);
				}
			}
			if (scratch > 0)
				buffer.put(scratch);
		}
		
		buffer.rewind();
		
		return Base64.getEncoder().encodeToString(buffer.array());
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
	 * Convert the given {@link Base64}-encoded String to a (non-jagged)
	 * {@code short[][]} with the given dimensions.
	 * 
	 * @param base64
	 * @param width
	 * @param height
	 * @return
	 */
	public default short[][] toShortArray(String base64, int width, int height) {
		
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
		
		final ByteBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(base64));
		
		buffer.rewind();
		
		final boolean[][] result = new boolean[width][height];
		for (int i = 0; i < width; i++) {
			byte scratch = 0;
			for (int j = 0; j < result[i].length; j++) {
				if (j % 8 == 0)
					scratch = buffer.get();
				result[i][j] = (scratch & (byte) 1) > 0 ? true : false;
			}
		}
		
		return new GreasedRegion(result);
	}
}