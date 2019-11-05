label = "move to point"
behavior = guarded(
	has([CanMove,HasLocation,IsMovingTo],[],[]),
	untilFail(
		sequence(
			invert(from("amAtPoint")),
			from("pathfindToPoint"),
			from("followMovementList")
			)
		)
	)