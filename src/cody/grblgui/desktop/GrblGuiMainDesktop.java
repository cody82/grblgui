package cody.grblgui.desktop;




import cody.grblgui.Main;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;


public class GrblGuiMainDesktop {
	static void displayHelp() {
		System.out.println("usage: java -jar grblgui.jar <gcode-file> <grbl-device-file>");
		System.out.println("\texample: java -jar grblgui.jar bunny.g /dev/ttyACM0");
	}
	public static void main (String[] argv) {
		if(argv.length != 2) {
			displayHelp();
			return;
		}
		Main main = new Main(argv[0], argv[1]);

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 2;
		config.width = 1280;
		config.height = 720;
		config.useGL20 = true;
		config.title = "grbl gui";
		//new JoglApplication(main, "grbl gui", 1280, 720, true);
		new LwjglApplication(main, config);
	}

}
