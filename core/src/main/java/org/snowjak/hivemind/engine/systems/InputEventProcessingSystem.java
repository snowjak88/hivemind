/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.engine.systems.input.BaseInputState;
import org.snowjak.hivemind.engine.systems.input.InputSystemState;
import org.snowjak.hivemind.events.input.InputEvent;
import org.snowjak.hivemind.gamescreen.InputEventListener;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.RegisterInputEventListener;
import org.snowjak.hivemind.gamescreen.updates.UnregisterInputEventListener;
import org.snowjak.hivemind.util.Profiler;
import org.snowjak.hivemind.util.Profiler.ProfilerTimer;

import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.gdx.ai.fsm.StackStateMachine;

/**
 * System responsible for handling {@link InputEvent}s. This system will use a
 * {@link StackStateMachine} to model transitions between input-contexts as
 * different {@link InputSystemState}s. Each such state will be responsible for
 * un-/registering {@link InputEventListener}s to handle such input-events as it
 * cares about.
 * 
 * @author snowjak88
 *
 */
public class InputEventProcessingSystem extends EntitySystem {
	
	private final StackStateMachine<InputEventProcessingSystem, InputSystemState> stateMachine;
	
	public InputEventProcessingSystem() {
		
		super();
		stateMachine = new StackStateMachine<InputEventProcessingSystem, InputSystemState>(this);
	}
	
	@Override
	public void update(float deltaTime) {
		
		final ProfilerTimer timer = Profiler.get().start("InputEventProcessingSystem (overall)");
		
		super.update(deltaTime);
		
		if (stateMachine.getCurrentState() == null)
			stateMachine.changeState(new BaseInputState());
		
		stateMachine.update();
		
		timer.stop();
	}
	
	public void registerListener(InputEventListener listener) {
		
		if (Context.getGameScreen() == null)
			return;
		
		final RegisterInputEventListener upd = GameScreenUpdatePool.get().get(RegisterInputEventListener.class);
		upd.setListener(listener);
		Context.getGameScreen().postGameScreenUpdate(upd);
	}
	
	public void unregisterListener(InputEventListener listener) {
		
		if (Context.getGameScreen() == null)
			return;
		
		final UnregisterInputEventListener upd = GameScreenUpdatePool.get().get(UnregisterInputEventListener.class);
		upd.setListener(listener);
		Context.getGameScreen().postGameScreenUpdate(upd);
	}
	
	public StackStateMachine<InputEventProcessingSystem, InputSystemState> getStateMachine() {
		
		return stateMachine;
	}
}
