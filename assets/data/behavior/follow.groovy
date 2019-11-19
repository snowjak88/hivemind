label = "wander"
behavior = loop(
		dynamic(
				from("moveToPoint/moveToPoint"),
				from("doNothing")
			)
		)