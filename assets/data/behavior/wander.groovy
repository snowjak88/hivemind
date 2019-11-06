label = "wander"
behavior = loop(
		sequence(
				from("pickRandomNearbyPoint"),
				from("moveToPoint/moveToPoint")
			)
		)