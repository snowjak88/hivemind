label = "an individual"
prefab = {
	
	def move = create(CanMove)
	move.speed = 2.5
	
	def see = create(CanSee)
	see.radius = 8
	
	def appearance = create(HasAppearance)
	appearance.ch = '@'
	appearance.color = Color.PINK
	
	create HasMap
	
	def track = create(LeavesTrack)
	track.prefabName = "smoke-cloud"
	
	create IsSelectable
	
	def behavior = create(HasBehavior)
	behavior.behaviorName = "follow"
	
}