label = "A cloud of smoke"
prefab = {
	
	def mat = create(IsMaterial)
	mat.materialName = "smoke"
	
	def wd = create(WillDissipate)
	wd.intervalRemaining = 5.0
	
	def ha = create(HasAppearance)
	ha.ch = '\u2592'
	
}