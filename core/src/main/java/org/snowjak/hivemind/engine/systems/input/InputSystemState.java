/**
 * 
 */
package org.snowjak.hivemind.engine.systems.input;

import org.snowjak.hivemind.engine.systems.InputEventProcessingSystem;

import com.badlogic.gdx.ai.fsm.State;

/**
 * The {@link InputEventProcessingSystem} uses a state-machine to provide unique
 * input-contexts. Each InputSystemState implementation represents a different
 * input-context.
 * 
 * @author snowjak88
 *
 */
public interface InputSystemState extends State<InputEventProcessingSystem> {
	
}
