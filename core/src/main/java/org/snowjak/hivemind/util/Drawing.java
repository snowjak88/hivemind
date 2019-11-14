/**
 * 
 */
package org.snowjak.hivemind.util;

import java.util.EnumSet;

import org.eclipse.collections.api.map.primitive.MutableObjectCharMap;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectCharHashMap;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.SparseTextMap;
import squidpony.squidmath.Coord;

/**
 * Utility methods to assist in drawing to GameScreen {@link SparseLayers
 * surface} and {@link SparseTextMap layers}.
 * 
 * @author snowjak88
 *
 */
public class Drawing {
	
	/**
	 * Draw a box on the given {@link SparseLayers surface}, using the desired
	 * {@link BoxStyle}.
	 * 
	 * @param style
	 * @param surface
	 * @param from
	 * @param to
	 * @param color
	 */
	public static void drawBox(BoxStyle style, SparseLayers surface, Coord from, Coord to, float color) {
		
		if (surface == null)
			return;
		
		drawBox(style, from, to, color, (loc, col, ch) -> surface.put(loc.x, loc.y, ch, col));
	}
	
	/**
	 * Draw a box on the given {@link SparseTextMap layer}, using the desired
	 * {@link BoxStyle}.
	 * 
	 * @param style
	 * @param layer
	 * @param from
	 * @param to
	 * @param color
	 */
	public static void drawBox(BoxStyle style, SparseTextMap layer, Coord from, Coord to, float color) {
		
		if (layer == null)
			return;
		
		drawBox(style, from, to, color, (loc, col, ch) -> layer.place(loc.x, loc.y, ch, col));
	}
	
	/**
	 * Draws a box using the given {@link DrawConsumer}.
	 * 
	 * @param style
	 * @param from
	 * @param to
	 * @param color
	 * @param drawer
	 */
	private static void drawBox(BoxStyle style, Coord from, Coord to, float color, DrawConsumer drawer) {
		
		if (style == null)
			return;
		if (from == null)
			return;
		if (to == null)
			return;
		if (drawer == null)
			return;
		
		if (from.equals(to))
			drawer.consume(from, color, '#');
		
		final int startX = (from.x < to.x) ? from.x : to.x;
		final int startY = (from.y < to.y) ? from.y : to.y;
		final int endX = (from.x > to.x) ? from.x : to.x;
		final int endY = (from.y > to.y) ? from.y : to.y;
		
		drawer.consume(Coord.get(startX, startY), color, style.getCharFor(BoxComponent.TOP_LEFT));
		drawer.consume(Coord.get(startX, endY), color, style.getCharFor(BoxComponent.BOTTOM_LEFT));
		drawer.consume(Coord.get(endX, startY), color, style.getCharFor(BoxComponent.TOP_RIGHT));
		drawer.consume(Coord.get(endX, endY), color, style.getCharFor(BoxComponent.BOTTOM_RIGHT));
		
		for (int x = startX + 1; x <= endX - 1; x++) {
			drawer.consume(Coord.get(x, startY), color, style.getCharFor(BoxComponent.HORIZONTAL));
			drawer.consume(Coord.get(x, endY), color, style.getCharFor(BoxComponent.HORIZONTAL));
		}
		
		for (int y = startY + 1; y <= endY - 1; y++) {
			drawer.consume(Coord.get(startX, y), color, style.getCharFor(BoxComponent.VERTICAL));
			drawer.consume(Coord.get(endX, y), color, style.getCharFor(BoxComponent.VERTICAL));
		}
	}
	
	/**
	 * Defines a unified box-drawing style.
	 * 
	 * @author snowjak88
	 *
	 */
	public enum BoxStyle {
		/**
		 * e.g.,
		 * 
		 * <pre>
		 * &#9484;&#9472;&#9516;&#9488;
		 * &#9500;&#9472;&#9532;&#9508;
		 * &#9492;&#9472;&#9524;&#9496;
		 * </pre>
		 */
		SINGLE_LINE('\u250C', '\u252C', '\u2510', '\u251C', '\u253C', '\u2524', '\u2514', '\u2534', '\u2518', '\u2500',
				'\u2502'),
		/**
		 * e.g.,
		 * 
		 * <pre>
		 * &#9556;&#9552;&#9574;&#9559;
		 * &#9568;&#9552;&#9580;&#9571;
		 * &#9562;&#9552;&#9577;&#9565;
		 * </pre>
		 */
		DOUBLE_LINE('\u2554', '\u2566', '\u2557', '\u2560', '\u256C', '\u2563', '\u255A', '\u2569', '\u255D', '\u2550',
				'\u2551');
		
		private final MutableObjectCharMap<BoxComponent> chars = new ObjectCharHashMap<>();
		
		private BoxStyle(char topLeft, char topMiddle, char topRight, char middleLeft, char middleMiddle,
				char middleRight, char bottomLeft, char bottomMiddle, char bottomRight, char horizontal,
				char vertical) {
			
			chars.put(BoxComponent.TOP_LEFT, topLeft);
			chars.put(BoxComponent.TOP_MIDDLE, topMiddle);
			chars.put(BoxComponent.TOP_RIGHT, topRight);
			chars.put(BoxComponent.MIDDLE_LEFT, middleLeft);
			chars.put(BoxComponent.MIDDLE_MIDDLE, middleMiddle);
			chars.put(BoxComponent.MIDDLE_RIGHT, middleRight);
			chars.put(BoxComponent.BOTTOM_LEFT, bottomLeft);
			chars.put(BoxComponent.BOTTOM_MIDDLE, bottomMiddle);
			chars.put(BoxComponent.BOTTOM_RIGHT, bottomRight);
			chars.put(BoxComponent.HORIZONTAL, horizontal);
			chars.put(BoxComponent.VERTICAL, vertical);
		}
		
		public char getCharFor(BoxComponent bc) {
			
			return chars.get(bc);
		}
	}
	
	public enum BoxComponent {
		TOP_LEFT(Direction.RIGHT, Direction.DOWN),
		TOP_MIDDLE(Direction.LEFT, Direction.RIGHT, Direction.DOWN),
		TOP_RIGHT(Direction.LEFT, Direction.DOWN),
		MIDDLE_LEFT(Direction.RIGHT, Direction.UP, Direction.DOWN),
		MIDDLE_MIDDLE(Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN),
		MIDDLE_RIGHT(Direction.LEFT, Direction.UP, Direction.DOWN),
		BOTTOM_LEFT(Direction.RIGHT, Direction.UP),
		BOTTOM_MIDDLE(Direction.LEFT, Direction.RIGHT, Direction.UP),
		BOTTOM_RIGHT(Direction.LEFT, Direction.UP),
		HORIZONTAL(Direction.LEFT, Direction.RIGHT),
		VERTICAL(Direction.UP, Direction.DOWN);
		
		private final EnumSet<Direction> connectivity = EnumSet.noneOf(Direction.class);
		
		private BoxComponent(Direction... connectivity) {
			
			for (int i = 0; i < connectivity.length; i++)
				this.connectivity.add(connectivity[i]);
		}
		
		/**
		 * Get the first BoxComponent whose "connectivity" matches the given set, or
		 * {@code null} if there is no exact match.
		 * 
		 * @param connectivity
		 * @return
		 */
		public static BoxComponent getBy(EnumSet<Direction> connectivity) {
			
			for (BoxComponent bc : BoxComponent.values())
				if (bc.connectivity.containsAll(connectivity) && connectivity.containsAll(bc.connectivity))
					return bc;
				
			return null;
		}
	}
	
	/**
	 * Provides a mechanism for abstracting the method for drawing a single
	 * character, in a foreground color, at a certain location.
	 * 
	 * @author snowjak88
	 *
	 */
	@FunctionalInterface
	private static interface DrawConsumer {
		
		public void consume(Coord location, float color, char ch);
	}
}
