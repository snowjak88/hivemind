label = "pick random nearby"
behavior = guarded(
		task { Status.SUCCEEDED },
		task { Status.RUNNING }
	)