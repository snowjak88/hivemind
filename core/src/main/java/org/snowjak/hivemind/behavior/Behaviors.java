/**
 * 
 */
package org.snowjak.hivemind.behavior;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import org.snowjak.hivemind.Context;
import org.snowjak.hivemind.RNG;
import org.snowjak.hivemind.TerrainTypes.TerrainType;
import org.snowjak.hivemind.concurrent.Executor;
import org.snowjak.hivemind.engine.Tags;
import org.snowjak.hivemind.engine.components.CanMove;
import org.snowjak.hivemind.engine.components.HasLocation;
import org.snowjak.hivemind.engine.components.HasMap;
import org.snowjak.hivemind.engine.components.HasMovementList;
import org.snowjak.hivemind.engine.components.HasPathfinder;
import org.snowjak.hivemind.engine.components.IsMovingTo;
import org.snowjak.hivemind.engine.components.NeedsUpdatedLocation;
import org.snowjak.hivemind.engine.systems.UniqueTagManager;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ai.GdxAI;
import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.badlogic.gdx.ai.btree.decorator.Repeat;
import com.badlogic.gdx.ai.btree.decorator.UntilFail;

import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;

/**
 * @author snowjak88
 *
 */
public class Behaviors {
	
	private static final Logger LOG = Logger.getLogger(Behaviors.class.getName());
	
	@SuppressWarnings("unchecked")
	public static BehaviorTree<Entity> getDefault() {
		
		return new BehaviorTree<>(new Repeat<>(new Sequence<>(pickRandomNearbyPoint(), moveToPoint())));
	}
	
	public static LeafTask<Entity> pickRandomNearbyPoint() {
		
		return new LeafTask<Entity>() {
			
			private Future<Coord> nearbyPointResult = null;
			
			@Override
			public Status execute() {
				
				if (getObject() == null)
					return Status.FAILED;
				
				if (nearbyPointResult != null) {
					Coord nearbyPoint = null;
					try {
						nearbyPoint = nearbyPointResult.get();
						nearbyPointResult = null;
					} catch (InterruptedException | ExecutionException e) {
						LOG.severe("Cannot select nearby point -- cannot retrieve result of search!");
					}
					
					if (nearbyPoint == null)
						return Status.FAILED;
					
					final IsMovingTo moveTo = Context.getEngine().createComponent(IsMovingTo.class);
					moveTo.setDestination(nearbyPoint);
					getObject().add(moveTo);
					return Status.SUCCEEDED;
				}
				
				if (!ComponentMapper.getFor(HasLocation.class).has(getObject()))
					return Status.FAILED;
				if (!ComponentMapper.getFor(HasMap.class).has(getObject()))
					return Status.FAILED;
				
				final HasLocation loc = ComponentMapper.getFor(HasLocation.class).get(getObject());
				final HasMap hasMap = ComponentMapper.getFor(HasMap.class).get(getObject());
				
				nearbyPointResult = Executor.get().submit((Callable<Coord>) () -> {
					final GreasedRegion floors = new GreasedRegion(hasMap.getMap().getSquidCharMap(), '#').not();
					final GreasedRegion known = hasMap.getMap().getKnown();
					final GreasedRegion nearby = new GreasedRegion(hasMap.getMap().getWidth(),
							hasMap.getMap().getHeight()).insert(loc.getLocation()).flood(floors.and(known), 8);
					return nearby.singleRandom(RNG.get());
				});
				return Status.RUNNING;
			}
			
			@Override
			protected Task<Entity> copyTo(Task<Entity> task) {
				
				return task;
			}
		};
	}
	
	/**
	 * Get the Task that models "move-to-point" behavior.
	 * 
	 * @return
	 */
	public static Task<Entity> moveToPoint() {
		
		final Task<Entity> notAtPoint = new Invert<>(entityAtMoveToPoint());
		final Task<Entity> pathfindToPoint = pathfindToMoveToPoint();
		final Task<Entity> followMovementList = followMovementList();
		
		@SuppressWarnings("unchecked")
		final Task<Entity> loopUntilFalse = new UntilFail<>(
				new Sequence<>(notAtPoint, pathfindToPoint, followMovementList));
		
		loopUntilFalse.setGuard(entityHasComponentGuard(IsMovingTo.class));
		
		return loopUntilFalse;
	}
	
	private static LeafTask<Entity> entityAtMoveToPoint() {
		
		return new LeafTask<Entity>() {
			
			@Override
			public Status execute() {
				
				if (getObject() == null)
					return Status.FAILED;
				if (!ComponentMapper.getFor(HasLocation.class).has(getObject()))
					return Status.FAILED;
				if (!ComponentMapper.getFor(IsMovingTo.class).has(getObject()))
					return Status.FAILED;
				final HasLocation loc = ComponentMapper.getFor(HasLocation.class).get(getObject());
				final IsMovingTo moveTo = ComponentMapper.getFor(IsMovingTo.class).get(getObject());
				
				return (loc.getLocation().equals(moveTo.getDestination())) ? Status.SUCCEEDED : Status.FAILED;
			}
			
			@Override
			protected Task<Entity> copyTo(Task<Entity> task) {
				
				return task;
			}
		};
	}
	
	private static LeafTask<Entity> pathfindToMoveToPoint() {
		
		return new LeafTask<Entity>() {
			
			private Future<List<Coord>> pathfindingResult = null;
			
			@Override
			public Status execute() {
				
				if (pathfindingResult != null)
					if (pathfindingResult.isDone()) {
						List<Coord> result = null;
						try {
							result = pathfindingResult.get();
							pathfindingResult = null;
						} catch (InterruptedException | ExecutionException e) {
							LOG.severe("Could not get pathfinding-result -- " + e.getClass().getSimpleName() + ": "
									+ e.getMessage());
						}
						if (result == null) {
							pathfindingResult = null;
							return Status.FAILED;
						}
						
						final HasMovementList movementList = Context.getEngine().createComponent(HasMovementList.class);
						movementList.setMovementList(result);
						getObject().add(movementList);
						return Status.SUCCEEDED;
					} else
						return Status.RUNNING;
					
				if (getObject() == null)
					return Status.FAILED;
				if (!ComponentMapper.getFor(HasLocation.class).has(getObject()))
					return Status.FAILED;
				if (!ComponentMapper.getFor(IsMovingTo.class).has(getObject()))
					return Status.FAILED;
				if (!ComponentMapper.getFor(HasMap.class).has(getObject()))
					return Status.FAILED;
					
				//
				// We don't yet have a pathfinder. We should soon.
				if (!ComponentMapper.getFor(HasPathfinder.class).has(getObject()))
					return Status.RUNNING;
				
				final HasLocation loc = ComponentMapper.getFor(HasLocation.class).get(getObject());
				final IsMovingTo moveTo = ComponentMapper.getFor(IsMovingTo.class).get(getObject());
				final HasPathfinder hasPathfinder = ComponentMapper.getFor(HasPathfinder.class).get(getObject());
				
				pathfindingResult = Executor.get().submit((Callable<List<Coord>>) () -> {
					hasPathfinder.getLock().acquireUninterruptibly();
					final List<Coord> result = hasPathfinder.getPathfinder().findPath(3, 16, null, null,
							loc.getLocation(), moveTo.getDestination());
					hasPathfinder.getLock().release();
					return result;
					
				});
				return Status.RUNNING;
			}
			
			@Override
			protected Task<Entity> copyTo(Task<Entity> task) {
				
				return task;
			}
		};
	}
	
	private static LeafTask<Entity> followMovementList() {
		
		return new LeafTask<Entity>() {
			
			private float timeRemaining = 0f;
			private boolean isMoving = false;
			
			@Override
			public Status execute() {
				
				if (getObject() == null)
					return Status.FAILED;
				if (!ComponentMapper.getFor(HasLocation.class).has(getObject()))
					return Status.FAILED;
				if (!ComponentMapper.getFor(HasMovementList.class).has(getObject()))
					return Status.FAILED;
				if (!ComponentMapper.getFor(CanMove.class).has(getObject()))
					return Status.FAILED;
				
				final HasLocation loc = ComponentMapper.getFor(HasLocation.class).get(getObject());
				final CanMove canMove = ComponentMapper.getFor(CanMove.class).get(getObject());
				final HasMovementList movementList = ComponentMapper.getFor(HasMovementList.class).get(getObject());
				
				//
				// If we're in the middle of moving from one cell to another --
				if (isMoving) {
					timeRemaining -= GdxAI.getTimepiece().getDeltaTime();
					
					if (timeRemaining <= 0f) {
						isMoving = false;
						timeRemaining = 0f;
						final NeedsUpdatedLocation needsUpdatedLocation = Context.getEngine()
								.createComponent(NeedsUpdatedLocation.class);
						needsUpdatedLocation.setNewLocation(movementList.getCurrentMovement());
						getObject().add(needsUpdatedLocation);
					}
					
					return Status.RUNNING;
				}
				
				//
				// We're not in the middle of moving from one cell to another.
				// That means we can initiate a new move, if we have one in the movement-list.
				//
				
				while (movementList.getCurrentMovement() != null
						&& movementList.getCurrentMovement().equals(loc.getLocation()))
					movementList.nextMovement();
				
				if (movementList.getCurrentMovement() == null) {
					getObject().remove(HasMovementList.class);
					return Status.SUCCEEDED;
				}
				
				//
				// If the current movement would place us inside a wall, we can't perform it.
				//
				final Entity worldMapEntity = Context.getEngine().getSystem(UniqueTagManager.class).get(Tags.WORLD_MAP);
				if (worldMapEntity == null)
					return Status.FAILED;
				final ComponentMapper<HasMap> hasMapMapper = ComponentMapper.getFor(HasMap.class);
				if (!hasMapMapper.has(worldMapEntity))
					return Status.FAILED;
				
				final TerrainType movingToTerrain = hasMapMapper.get(worldMapEntity).getMap()
						.getTerrain(movementList.getCurrentMovement());
				if (movingToTerrain == null || movingToTerrain.getSquidChar() == '#')
					return Status.FAILED;
					
				//
				// We have a movement ready. So we need to wait a time sufficient to move us
				// there.
				// We assume that each movement will only ever be 1 cell in length.
				// This means that we can simply take the reciprocal of our configured speed.
				//
				final float timeToMove = (canMove.getSpeed() > 0f) ? 1f / canMove.getSpeed() : 0f;
				
				isMoving = true;
				timeRemaining = timeToMove;
				
				return Status.RUNNING;
			}
			
			@Override
			protected Task<Entity> copyTo(Task<Entity> task) {
				
				return task;
			}
		};
	}
	
	private static LeafTask<Entity> entityHasComponentGuard(Class<? extends Component> clazz) {
		
		return new LeafTask<Entity>() {
			
			@Override
			public Status execute() {
				
				return (getObject() != null && ComponentMapper.getFor(clazz).has(getObject())) ? Status.SUCCEEDED
						: Status.FAILED;
			}
			
			@Override
			protected Task<Entity> copyTo(Task<Entity> task) {
				
				return task;
			}
		};
	}
}
