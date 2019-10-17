package org.snowjak.hivemind.lwjgl3;

import org.snowjak.hivemind.App;
import org.snowjak.hivemind.config.Config;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
	
	public static void main(String[] args) {
		
		createApplication();
	}
	
	private static Lwjgl3Application createApplication() {
		
		return new Lwjgl3Application(new App(), getConfiguration());
	}
	
	private static Lwjgl3ApplicationConfiguration getConfiguration() {
		
		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		
		configuration.setTitle("hivemind");
		configuration.setWindowedMode(Config.get().getInt(App.PREFERENCE_WINDOW_WIDTH),
				Config.get().getInt(App.PREFERENCE_WINDOW_HEIGHT));
		configuration.setResizable(false);
		configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
		
		return configuration;
	}
}