package cody.grblgui.desktop;




import cody.grblgui.Main;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;


public class GrblGuiMainDesktop {
	static void displayHelp() {
		System.out.println("usage: java -jar grblgui.jar <gcode-file> <grbl-device-file> [<tool-size-in-mm>]");
		System.out.println("\texample: java -jar grblgui.jar bunny.g /dev/ttyACM0 3");
	}
	public static void main (String[] argv) {
		if(argv.length != 2 && argv.length != 3) {
			displayHelp();
			return;
		}
		
		float toolsize = -1;
		if(argv.length == 3)
			toolsize = Float.parseFloat(argv[2]);
		
		Main main = new Main(argv[0], argv[1], toolsize);
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.samples = 2;
		config.width = 1280;
		config.height = 720;
		config.useGL20 = true;
		config.title = "grbl gui";
		new LwjglApplication(main, config);
	}

}
