label = "pathfind to point"
behavior = guarded(
		has([HasLocation, IsMovingTo, HasPathfinder, HasMap]),
		task(
		start: {
			prop["pathfind-task"] = null
		},
		exec: {
			try {
				
				if(prop["pathfind-task"] != null) {
					def pathfindTask = prop["pathfind-task"]
					if(!pathfindTask.isDone())
						return Status.RUNNING
					
					def result = pathfindTask.get()
					prop["pathfind-task"] = null
					
					if(result == null || result.isEmpty())
						return Status.FAILED
					
					def hml = create(HasMovementList)
					hml.setMovementList result
					
					return Status.SUCCEEDED
				}
				
				def loc = get(HasLocation)
				def moveTo = get(IsMovingTo)
				def map = get(HasMap)
				
				if(moveTo.destination == null)
					return Status.FAILED
				
				def pathfinder = get(HasPathfinder)
				if(pathfinder.pathfinder == null)
					return Status.RUNNING
				
				def known = map.map.known
				def unknown = prop['unknown']
				if(unknown == null || unknown.width != known.width || unknown.height != known.height)
					unknown = new Region(known).not()
				else
					unknown.remake(known).not()
				
				prop['unknown'] = unknown
				
				prop["pathfind-task"] = schedule({
					pathfinder.lock.lock()
					def result = pathfinder.pathfinder.findPath(3, -1, unknown, null, loc.location, moveTo.destination)
					pathfinder.lock.unlock()
					result
				})
				
				return Status.RUNNING
				
			} catch(Throwable t) {
				println "Exception while pathfinding -- ${t.class.simpleName} -- ${t.getMessage()}"
				t.printStackTrace()
				return Status.FAILED
			}
		}))