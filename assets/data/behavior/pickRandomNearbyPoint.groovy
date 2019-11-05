import java.util.concurrent.Callable
import java.util.concurrent.Future
import org.snowjak.hivemind.concurrent.Executor
import org.snowjak.hivemind.RNG
import squidpony.squidmath.Coord
import squidpony.squidmath.GreasedRegion

label = "pick random nearby"
behavior = task {
	try {
		
		if(prop["nearbyPointResult"] != null) {
			Future<Coord> nearbyPointResult = prop["nearbyPointResult"]
			
			if(!nearbyPointResult.isDone())
				return Status.RUNNING
			
			Coord nearbyPoint = nearbyPointResult.get()
			
			def imt = create(IsMovingTo)
			imt.destination = nearbyPoint
			
			prop["nearbyPointResult"] = null
			
			return Status.SUCCEEDED
		}
		
		if(!has(HasLocation))
			return Status.FAILED
		if(!has(HasMap))
			return Status.FAILED
		
		def loc = get(HasLocation)
		def hm = get(HasMap)
		
		prop["nearbyPointResult"] = Executor.get().submit({
			def floors = new GreasedRegion(hm.getMap().getSquidCharMap(), (char) '#').not()
			def known = hm.getMap().getKnown()
			def nearby = new GreasedRegion(hm.getMap().getWidth(), hm.getMap().getHeight())
							.insert(loc.getLocation())
							.flood(floors.and(known), 8)
			return nearby.singleRandom(RNG.get())
		} as Callable<Coord>)
		
		return Status.RUNNING
		
	} catch (Throwable t) {
		
		return Status.FAILED
	}
}