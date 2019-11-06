label = "pick random nearby"
behavior = task {
	try {
		
		if(prop["nearbyPoint-task"] != null) {
			nearbyPointResult = prop["nearbyPoint-task"]
			
			if(!nearbyPointResult.isDone())
				return Status.RUNNING
			
			def nearbyPoint = nearbyPointResult.get()
			
			def imt = create(IsMovingTo)
			imt.destination = nearbyPoint
			
			prop["nearbyPoint-task"] = null
			
			return Status.SUCCEEDED
		}
		
		if(!has(HasLocation))
			return Status.FAILED
		if(!has(HasMap))
			return Status.FAILED
		
		def loc = get(HasLocation)
		def hm = get(HasMap)
		def myMap = hm.getMap()
		
		if(myMap == null)
			return Status.RUNNING
		
		prop["nearbyPoint-task"] = schedule({
			def floors = prop["floors-cache"]
			if(floors == null) {
				floors = new GreasedRegion(myMap.getWidth(), myMap.getHeight())
				prop["floors-cache"] = floors
			}
			floors.refill(myMap.getSquidCharMap(), (char) '#').not()
			
			def known = myMap.getKnown()
			
			def nearby = prop["nearby-cache"]
			if(nearby == null) {
				nearby = new GreasedRegion(myMap.getWidth(), myMap.getHeight())
				prop["nearby-cache"] = nearby
			}
			nearby.fill(false)
					.insert(loc.getLocation())
					.flood(floors.and(known), 8)
			
			nearby.singleRandom(RNG.get())
		})
		
		return Status.RUNNING
		
	} catch (Throwable t) {
		println "Exception while picking random point -- ${t.class.simpleName} -- ${t.getMessage()}"
		return Status.FAILED
	}
}