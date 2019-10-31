/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.systems.input.BaseInputState;
import org.snowjak.hivemind.engine.systems.input.InputSystemState;
import org.snowjak.hivemind.events.input.InputEvent;
import org.snowjak.hivemind.gamescreen.InputEventListener;

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
		
		stateMachine.changeState(new BaseInputState());
		
	}
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		
		stateMachine.update();
	}
	
	public StackStateMachine<InputEventProcessingSystem, InputSystemState> getStateMachine() {
		
		return stateMachine;
	}
}
