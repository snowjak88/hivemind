/**
 * 
 */
package org.snowjak.hivemind.engine.systems;

import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasUpdatedLocation;
import org.snowjak.hivemind.engine.components.LeavesTrack;
import org.snowjak.hivemind.engine.prefab.PrefabScript;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * When an Entity that {@link LeavesTrack leaves a track}
 * {@link HasUpdatedLocation changes its location}, this system will instantiate
 * a track-Entity at its old location.
 * 
 * @author snowjak88
 *
 */
public class TrackLeavingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasUpdatedLocation> HAS_UPDATED = ComponentMapper
			.getFor(HasUpdatedLocation.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<LeavesTrack> LEAVES_TRACK = ComponentMapper.getFor(LeavesTrack.class);
	
	public TrackLeavingSystem() {
		
		super(Family.all(HasUpdatedLocation.class, LeavesTrack.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final HasUpdatedLocation updated = HAS_UPDATED.get(entity);
		final HasLocation location = HAS_LOCATION.get(entity);
		
		if (updated.getOldLocation() == null)
			return;
		
		if (updated.getOldLocation() == location.getLocation())
			return;
		
		final LeavesTrack leavesTrack = LEAVES_TRACK.get(entity);
		
		if (leavesTrack.getPrefabName() == null)
			return;
		
		final PrefabScript trackPrefab = PrefabScript.byName(leavesTrack.getPrefabName());
		if (trackPrefab == null)
			return;
		
		final Entity track = trackPrefab.run();
		
		final HasLocation trackLocation = getEngine().createComponent(HasLocation.class);
		trackLocation.setLocation(updated.getOldLocation());
		track.add(trackLocation);
	}
}
