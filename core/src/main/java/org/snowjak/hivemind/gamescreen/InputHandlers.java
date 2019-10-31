/**
 * 
 */
package org.snowjak.hivemind.gamescreen;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.snowjak.hivemind.events.input.InputEvent;
import org.snowjak.hivemind.gamescreen.InputEventListener.InputEventHandlers;
import org.snowjak.hivemind.util.lambda.TriConsumer;

import squidpony.squidmath.Coord;

/**
 * Helper-class with functions designed to aggregate common input-handling
 * scenarios.
 * <p>
 * To use, provide as part of InputEventListener construction -- e.g.
 * 
 * <pre>
 * InputEventListener.{@link InputEventListener#build() build()}.{@link InputEventListener.Builder#handlers(InputEventHandlers) handlers(...)}
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class InputHandlers {
	
	/**
	 * Allows you to detect and respond to drag events -- i.e., to record:
	 * <ul>
	 * <li>When the event begins</li>
	 * <li>When the event is updated, and the current start- and end-locations for
	 * the cursor</li>
	 * <li>When the event completes, and the final start- and end-locations for the
	 * cursor</li>
	 * </ul>
	 * 
	 * @param onDragStart
	 *            consumes the {@link InputEvent} when this event-handler is
	 *            activated; may be {@code null}
	 * @param onDragUpdate
	 *            consumes the current {@link InputEvent}, along with the starting
	 *            and current cursor-locations (expressed in world-, not
	 *            screen-coordinates); may be {@code null}
	 * @param onDragFinish
	 *            consumes the final {@link InputEvent}, along with the starting and
	 *            ending cursor-locations (expressed in world-, not
	 *            screen-coordinates)
	 * @return
	 */
	public static InputEventHandlers drag(Consumer<InputEvent> onDragStart,
			TriConsumer<InputEvent, Coord, Coord> onDragUpdate, TriConsumer<InputEvent, Coord, Coord> onDragFinish) {
		
		final AtomicReference<Coord> startDrag = new AtomicReference<>(), endDrag = new AtomicReference<>(),
				prevDrag = new AtomicReference<>();
		
		final Consumer<InputEvent> event = (e) -> {
			if (startDrag.get() == null) {
				
				if (onDragStart != null)
					onDragStart.accept(e);
				
				startDrag.set(e.getMapCursor());
				
			} else {
				
				endDrag.set(e.getMapCursor());
				
				if (onDragUpdate != null)
					if (prevDrag.get() == null || prevDrag.get().x != e.getMapCursor().x
							|| prevDrag.get().y != e.getMapCursor().y) {
						onDragUpdate.accept(e, startDrag.get(), endDrag.get());
						prevDrag.set(e.getMapCursor());
					}
			}
		};
		
		final Consumer<InputEvent> endEvent = (e) -> {
			if (onDragFinish != null)
				onDragFinish.accept(e, startDrag.get(), endDrag.get());
			
			startDrag.set(null);
			endDrag.set(null);
			prevDrag.set(null);
		};
		
		return new InputEventHandlers(event, endEvent);
	}
}
