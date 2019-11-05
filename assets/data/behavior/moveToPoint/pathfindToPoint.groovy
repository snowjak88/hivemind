import java.util.List
import java.util.concurrent.Callable
import java.util.concurrent.Future
import org.snowjak.hivemind.concurrent.Executor
import squidpony.squidmath.Coord

label = "pathfind to point"
behavior = task {
	
	try {
		
		if(prop["pathfind-result"] != null) {
			def pathfindFuture = (Future<List<Coord>>) prop["pathfind-result"]
			if(!pathfindFuture.isDone())
				return Status.RUNNING
			
			def result = pathfindFuture.get()
			prop["pathfind-result"] = null
			
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
		
		def pathfindFuture = Executor.get().submit({
			pathfinder.lock.acquireUninterruptibly()
			def result = pathfinder.pathfinder.findPath(3, 16, null, null, loc.location, moveTo.destination)
			pathfinder.lock.release()
			result
		} as Callable<List<Coord>>)
		prop["pathfind-result"] = pathfindFuture
		
		return Status.RUNNING;
	} catch(Throwable t) {
		println "Exception while pathfinding -- ${t.class.simpleName} -- ${t.getMessage()}"
		return Status.FAILED
	}
}