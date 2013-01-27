package cody.grblgui;


import com.badlogic.gdx.Game;

public class Main extends Game {
	String filename;
	String device;
	float toolsize;
	public Main(String _filename, String _device, float _toolsize) {
		filename = _filename;
		device = _device;
		toolsize = _toolsize;
	}
	
	@Override
	public void create() {
		this.setScreen(new MainScreen(filename, device, toolsize));
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
