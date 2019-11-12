label = "MIXIN: positioned at random floor-cell"
prefab = {
	
	def loc = create(HasLocation)
	def floors = new Region(worldMap().getMap().getSquidCharMap(), (char) '.')
	loc.location = floors.singleRandom(RNG.get())
	
}