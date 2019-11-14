label = "wanderer"
prefab = {
	
	faction "player"
	
	def move = create(CanMove)
	move.speed = 2.5
	
	def see = create(CanSee)
	see.radius = 8
	
	def appearance = create(HasAppearance)
	appearance.ch = '@'
	appearance.color = Color.PINK
	
	create(HasMap)
	
	def behavior = create(HasBehavior)
	behavior.behaviorName = "wander"
	
	def track = create(LeavesTrack)
	track.prefabName = "smoke-cloud"
	
}