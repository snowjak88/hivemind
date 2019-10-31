/**
 * 
 */
package org.snowjak.hivemind.gamescreen.updates;

import java.util.function.Consumer;

import org.snowjak.hivemind.gamescreen.GameScreen;

import squidpony.squidgrid.gui.gdx.SparseTextMap;

/**
 * Allows you to provide an arbitrary lambda-expression to draw to the layer
 * associated with the given name.
 * 
 * @author snowjak88
 *
 */
public class LayerUpdate implements GameScreenUpdate {
	
	private String name = null;
	private Consumer<SparseTextMap> procedure = null;
	
	public String getName() {
		
		return name;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
	
	public Consumer<SparseTextMap> getProcedure() {
		
		return procedure;
	}
	
	public void setProcedure(Consumer<SparseTextMap> procedure) {
		
		this.procedure = procedure;
	}
	
	@Override
	public void execute(GameScreen gameScreen) {
		
		if (getProcedure() != null && name != null)
			procedure.accept(gameScreen.getNamedLayer(name));
	}
	
	@Override
	public void reset() {
		
		procedure = null;
		name = null;
	}
}
