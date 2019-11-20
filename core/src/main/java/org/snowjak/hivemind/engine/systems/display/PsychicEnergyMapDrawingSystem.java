/**
 * 
 */
package org.snowjak.hivemind.engine.systems.display;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.Tags;
import org.snowjak.hivemind.engine.components.CanSensePsychicEnergy;
import org.snowjak.hivemind.engine.systems.manager.UniqueTagManager;
import org.snowjak.hivemind.gamescreen.updates.FreeLayer;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdate;
import org.snowjak.hivemind.gamescreen.updates.GameScreenUpdatePool;
import org.snowjak.hivemind.gamescreen.updates.LayerUpdate;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * When active, this system will issue {@link GameScreenUpdate}s sufficient to
 * draw the {@link Tags#POV POV-entity's} psychic-sense map, if available.
 * <p>
 * Note that, by default, this system is <em>not<em> active.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PsychicEnergyMapDrawingSystem extends EntitySystem {
	
	private static final String LAYER_NAME = PsychicEnergyMapDrawingSystem.class.getSimpleName();
	private static final float LAYER_ALPHA = 0.5f;
	private static final float BLANK_COLOR_FLOAT = SColor.multiplyAlpha(SColor.AURORA_TRANSPARENT.toFloatBits(),
			LAYER_ALPHA), ENERGETIC_COLOR_FLOAT = SColor.multiplyAlpha(SColor.AURORA_CREAM.toFloatBits(), LAYER_ALPHA);
	
	private static final ComponentMapper<CanSensePsychicEnergy> CAN_SENSE = ComponentMapper
			.getFor(CanSensePsychicEnergy.class);
	
	private final BlockingQueue<float[]> scratch = new LinkedBlockingQueue<>();
	
	public PsychicEnergyMapDrawingSystem() {
		
		super();
		this.setProcessing(false);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		super.removedFromEngine(engine);
		
		if (Context.getGameScreen() != null) {
			final FreeLayer upd = GameScreenUpdatePool.get().get(FreeLayer.class);
			upd.setName(LAYER_NAME);
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
	}
	
	@Override
	public void update(float deltaTime) {
		
		super.update(deltaTime);
		
		if (Context.getGameScreen() == null)
			return;
		
		final Entity povEntity = getEngine().getSystem(UniqueTagManager.class).get(Tags.POV);
		if (povEntity == null || !CAN_SENSE.has(povEntity) || CAN_SENSE.get(povEntity).getMap() == null)
			return;
		
		final CanSensePsychicEnergy sense = CAN_SENSE.get(povEntity);
		
		{
			final LayerUpdate upd = GameScreenUpdatePool.get().get(LayerUpdate.class);
			upd.setName(LAYER_NAME);
			upd.setProcedure(stm -> stm.clear());
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
		
		for (int x = 0; x < sense.getMap().length; x++) {
			
			float[] columnColors = scratch.poll();
			
			if (columnColors == null || columnColors.length != sense.getMap()[x].length)
				columnColors = new float[sense.getMap()[x].length];
			
			for (int y = 0; y < sense.getMap()[x].length; y++) {
				final double energy = sense.getMap()[x][y];
				columnColors[y] = SColor.lerpFloatColors(BLANK_COLOR_FLOAT, ENERGETIC_COLOR_FLOAT, (float) energy);
			}
			
			{
				final int _x = x;
				final float[] colors = columnColors;
				
				final LayerUpdate upd = GameScreenUpdatePool.get().get(LayerUpdate.class);
				upd.setName(LAYER_NAME);
				upd.setProcedure(stm -> {
					for (int y = 0; y < colors.length; y++)
						stm.place(_x, y, '\u2593', colors[y]);
					scratch.offer(colors);
				});
				Context.getGameScreen().postGameScreenUpdate(upd);
			}
		}
	}
	
	public void activate() {
		
		this.setProcessing(true);
	}
	
	public void deactivate() {
		
		this.setProcessing(false);
		
		if (Context.getGameScreen() != null) {
			final LayerUpdate upd = GameScreenUpdatePool.get().get(LayerUpdate.class);
			upd.setName(LAYER_NAME);
			upd.setProcedure(stm -> stm.clear());
			Context.getGameScreen().postGameScreenUpdate(upd);
		}
	}
}
