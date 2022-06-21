package me.flerpharos.games.maps.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import me.flerpharos.games.maps.Maps;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();

		config.width = 1280;
		config.height = 720;
		config.title = "Attack of the Blobbos";

		config.addIcon("aotb.png", Files.FileType.Internal);
		//config.set

		new LwjglApplication(new Maps(), config);
	}
}
