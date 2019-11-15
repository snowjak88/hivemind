label = "MIXIN: randomly walks the map"
prefab = {
	
	create(HasMap)
	create(CanMove)
	
	def behavior = create(HasBehavior)
	behavior.behaviorName = "wander"
	
}