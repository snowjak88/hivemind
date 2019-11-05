label = "am at point"
behavior = task {
	if(!has(HasLocation))
		return Status.FAILED
	if(!has(IsMovingTo))
		return Status.FAILED
	
	def loc = get(HasLocation)
	def moveTo = get(IsMovingTo)
	
	loc.location.equals(moveTo.destination) ? Status.SUCCEEDED : Status.FAILED
}