label = "A cloud of smoke"
prefab = {
	
	def mat = create(IsMaterial)
	mat.materialName = "smoke"
	
	def ha = create(HasAppearance)
	ha.ch = '\u2592'
	
}