label = "move to point"
behavior = guarded(
	has([CanMove,HasLocation,IsMovingTo]),
	untilFail(
		sequence(
			invert(from("moveToPoint/amAtPoint")),
			from("moveToPoint/pathfindToPoint"),
			from("moveToPoint/followMovementList")
			)
		)
	)