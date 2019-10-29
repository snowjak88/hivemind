/**
 * 
 */
package org.snowjak.hivemind.engine;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.concurrent.PerFrameProcess;

/**
 * A {@link PerFrameProcess} which updates the {@link Engine} with every frame.
 * 
 * @author snowjak88
 *
 */
public class EngineUpdatePerFrameProcess extends PerFrameProcess {
	
	@Override
	public void starting() {
		
		//
	}
	
	@Override
	public void processFrame(float delta) {
		
		Context.getEngine().update(delta);
	}
	
	@Override
	public void stopping() {
		
		//
	}
	
}
