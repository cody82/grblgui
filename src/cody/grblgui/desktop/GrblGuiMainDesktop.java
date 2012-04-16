package cody.grblgui.desktop;




import cody.grblgui.Main;

import com.badlogic.gdx.backends.jogl.JoglApplication;

public class GrblGuiMainDesktop {
	public static void main (String[] argv) {
		
		Main main = new Main(argv[0], argv[1]);

		
		new JoglApplication(main, "grbl gui", 1280, 720, true);
	}

}
