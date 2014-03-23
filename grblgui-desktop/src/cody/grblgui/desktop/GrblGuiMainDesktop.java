package cody.grblgui.desktop;




import cody.grbl.GrblFactory;
import cody.grbl.GrblStreamFactory;
import cody.grblgui.Main;
import cody.grblgui.ScalaSimulationConverter;
import cody.grblgui.SimulationConverter;

import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;


public class GrblGuiMainDesktop {
	static void displayHelp() {
		System.out.println("usage: java -jar grblgui.jar [<gcode-file-or-directory> [<grbl port>]]");
		System.out.println("\texample: java -jar grblgui.jar /home/cody/gcode/ /dev/ttyACM0");
	}
	public static void main (String[] argv) {
		/*if(argv.con) {
			displayHelp();
			return;
		}*/
		
		String dir = null;
		String port = null;
		if(argv.length > 0) {
			dir = argv[0];
			if(argv.length > 1) {
				port = argv[1];
			}
		}
		
		GrblStreamFactory.instance = new GrblFactory();
		SimulationConverter.instance = new ScalaSimulationConverter();
		
		Main main = new Main(dir, port);
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.vSyncEnabled = true;
		config.samples = 2;
		config.width = 1280;
		config.height = 720;
		config.useGL20 = true;
		config.title = "grbl gui";
		new LwjglApplication(main, config);
	}

}
