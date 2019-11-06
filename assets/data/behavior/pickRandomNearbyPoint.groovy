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
		
		prop["nearbyPoint-task"] = schedule({
			def floors = new GreasedRegion(hm.getMap().getSquidCharMap(), (char) '#').not()
			def known = hm.getMap().getKnown()
			def nearby = new GreasedRegion(hm.getMap().getWidth(), hm.getMap().getHeight())
							.insert(loc.getLocation())
							.flood(floors.and(known), 8)
			nearby.singleRandom(RNG.get())
		})
		
		return Status.RUNNING
		
	} catch (Throwable t) {
		
		return Status.FAILED
	}
}