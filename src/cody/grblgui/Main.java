package cody.grblgui;

import java.io.IOException;

import cody.gcode.GCodeFile;
import cody.gcode.GCodeParser;
import cody.grbl.GrblConnection;
import cody.grbl.GrblStream;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

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
