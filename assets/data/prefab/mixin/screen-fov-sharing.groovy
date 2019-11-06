label = "shares FOV with the screen"
prefab = {
	def see = create(CanSee)
	see.radius = 6
	
	create(HasMap)
	
	def copiesFov = create(CopiesFOVTo)
	copiesFov.copyTo = id(tagged(Tags.SCREEN_MAP))
}