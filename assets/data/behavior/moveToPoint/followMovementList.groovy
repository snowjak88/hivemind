import com.badlogic.gdx.ai.GdxAI
import org.snowjak.hivemind.TerrainTypes.TerrainType

label = "follow movement list"
behavior =
		guarded(
			has([HasLocation, HasMovementList, CanMove]), 
			task(
				start: {
					prop["isMoving"] = false
					prop["timeRemaining"] = 0f
				},
				exec: {
					def loc = get(HasLocation)
					def cm = get(CanMove)
					def ml = get(HasMovementList)
					
					def isMoving = prop["isMoving"] ?: false
					def timeRemaining = prop["timeRemaining"] ?: 0f
					
					if(isMoving) {
						timeRemaining -= GdxAI.getTimepiece().getDeltaTime()
						
						if(timeRemaining <= 0) {
							isMoving = false
							timeRemaining = 0
							def nul = create(NeedsUpdatedLocation)
							nul.setNewLocation ml.getCurrentMovement()
						}
						
						prop["isMoving"] = isMoving
						prop["timeRemaining"] = timeRemaining
						
						return Status.RUNNING
					}
					
					while(ml.getCurrentMovement() != null && ml.getCurrentMovement().equals(loc.location))
						ml.nextMovement()
					
					if(ml.getCurrentMovement() == null) {
						remove(HasMovementList)
						return Status.SUCCEEDED
					}
					
					def worldMap = worldMap()
					if(worldMap == null)
						return Status.FAILED
					
					def tt = worldMap.getMap().getTerrain(ml.getCurrentMovement())
					if(tt == null || tt.squidChar == '#') {
						remove(HasMovementList)
						return Status.SUCCEEDED
					}
					
					def timeToMove = (cm.speed <= 0) ? 0 : 1.0 / cm.speed
					
					prop["isMoving"] = true
					prop["timeRemaining"] = timeToMove
					
					Status.RUNNING
				}))