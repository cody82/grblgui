package cody.grblgui;


import com.badlogic.gdx.Game;

public class Main extends Game {
	String filename;
	String device;
	public Main(String _filename, String _device) {
		filename = _filename;
		device = _device;
	}
	
	@Override
	public void create() {
		this.setScreen(new MainScreen(filename, device));
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}
	
	
	@Override
	public void render() {
		super.render();
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}

}
