label = "MIXIN: shares FOV with the screen"
prefab = {
	def copiesFov = create(CopiesFOVTo)
	copiesFov.copyTo = id(tagged(Tags.SCREEN_MAP))
	copiesFov.radius = 16
}