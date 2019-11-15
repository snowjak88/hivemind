label = "pathfind to point"
behavior = task {
	
	try {
		
		if(prop["pathfind-task"] != null) {
			def pathfindTask = prop["pathfind-task"]
			if(!pathfindTask.isDone())
				return Status.RUNNING
			
			def result = pathfindTask.get()
			prop["pathfind-task"] = null
			
			if(result == null)
				return Status.FAILED
			
			def hml = create(HasMovementList)
			hml.setMovementList result
			
			return Status.SUCCEEDED
		}
		
		if(!has(HasLocation))
			return Status.FAILED
		if(!has(IsMovingTo))
			return Status.FAILED
		if(!has(HasMap))
			return Status.FAILED
		
		def loc = get(HasLocation)
		def moveTo = get(IsMovingTo)
		def map = get(HasMap)
		
		if(!has(HasPathfinder))
			return Status.RUNNING
		
		def pathfinder = get(HasPathfinder)
		
		prop["pathfind-task"] = schedule({
			pathfinder.lock.acquireUninterruptibly()
			def result = pathfinder.pathfinder.findPath(128, -1, null, null, loc.location, moveTo.destination)
			pathfinder.lock.release()
			result
		})
		
		return Status.RUNNING;
	} catch(Throwable t) {
		println "Exception while pathfinding -- ${t.class.simpleName} -- ${t.getMessage()}"
		return Status.FAILED
	}
}