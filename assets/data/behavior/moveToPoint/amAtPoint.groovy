label = "am at point"
behavior = task {
	if(!has(HasLocation))
		return Status.FAILED
	if(!has(IsMovingTo))
		return Status.FAILED
	
	def loc = get(HasLocation)
	def moveTo = get(IsMovingTo)
	
	def amAtPoint = loc.location.equals(moveTo.destination)
	
	if(amAtPoint)
		remove(IsMovingTo)
	
	(amAtPoint) ? Status.SUCCEEDED : Status.FAILED
}