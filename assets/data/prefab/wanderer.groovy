label = "wanderer"
prefab = {
	
	def move = create(CanMove)
	move.speed = 1.5
	
	def see = create(CanSee)
	see.radius = 6
	
	def appearance = create(HasAppearance)
	appearance.ch = '@'
	appearance.color = Color.AURORA_APRICOT
	
	create(HasMap)
	
	def behavior = create(HasBehavior)
	behavior.behaviorName = "wander"
	
}