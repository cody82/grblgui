package cody.grblgui;

import cody.gcode.GCodeFile;
import cody.grbl.GrblStreamFactory;
import cody.grbl.GrblStreamInterface;
import cody.grbl.GrblStreamListener;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class MainScreen implements Screen {

	GrblStreamInterface grbl;
	
	Workspace workspace;
	PerspectiveCamera camera;
	OrthographicCamera orthocam;
	Tool tool;
	Tool current;
	Toolpath toolpath;
	public Part part;
	public boolean draw_part;
	
	SpriteBatch spriteBatch;
	BitmapFont font;
	GCodeFile file;
	public String filename;
	String device;

	ConsoleWindow console;
	
    Skin skin;
    Stage ui;
	
	float postimer;
	Vector3 lastpos = new Vector3(0,0,0);
	
	float speed;
	float toolsize;
	
	public MainScreen(String _filename, String _device) {
		filename = _filename;
		device = _device;
	}
	
	@Override
	public void dispose() {
		if(grbl != null) {
			grbl.dispose();
			grbl = null;
		}
	}

	@Override
	public void hide() {
		if(grbl != null) {
			grbl.dispose();
			grbl = null;
		}
	}

	@Override
	public void pause() {
	}

	boolean ztest = true;
	int lastline = -1;
	
	@Override
	public void render(float arg0) {
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);
		Gdx.gl20.glDepthMask(true);
		if(ztest)
			Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		else
			Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
		//Gdx.gl20.glDisable(GL20.GL_BLEND);
		//Gdx.gl20.glLineWidth(2);
		
		float t = arg0;
		postimer+=t;

		if(grbl != null) {
			Vector3 tooltargetpos = grbl.getToolPosition().cpy();
			Vector3 d = tooltargetpos.sub(current.position);
	
			Vector3 result = current.position.add(d.mul(Math.min(t * 10f, 1f)));
			current.position = result;
		}
		//camera.rotate(1, 0, 1, 1);
		//camera.lookAt(current.position.x, current.position.y, current.position.z);
		camera.update(true);
		

		if(postimer >= 1f) {
			speed = current.position.dst(lastpos) / postimer * 60f;
			lastpos = current.position.cpy();
			postimer = 0;
		}

		Matrix4 matrix = camera.combined.cpy();

		if(part != null && draw_part)
			part.draw(matrix);

		workspace.draw(matrix);
		if(toolpath != null)
			toolpath.draw(matrix);
		tool.draw(matrix);
		current.draw(matrix);

		orthocam.update();
		
		if(file != null && grbl != null) {
			spriteBatch.setProjectionMatrix(orthocam.projection);
			spriteBatch.setTransformMatrix(orthocam.view);
			spriteBatch.begin();
			int currentline = toolpath.currentLine = grbl.isStreaming() ? grbl.getCurrentLine() : -1;
			
			
			if(lastline < 0)
				lastline = 0;
			for(int i = lastline; i <= currentline; ++i) {
				console.writeLine(file.gcode.get(i).getContent(grbl.getSpeed()));
			}
			lastline = currentline + 1;
				
			font.draw(spriteBatch, "status: " + (grbl.isStreaming() ? "streaming " : "") + (grbl.isHold() ? "hold" : "running"), Gdx.graphics.getWidth() - 220, 80);
			font.draw(spriteBatch, "speed: " + Float.toString(speed)+"mm/min", Gdx.graphics.getWidth() - 220, 40);
			if(grbl.isStreaming()) {
				font.draw(spriteBatch, "eta:" + Float.toString(toolpath.getEta())+"min", Gdx.graphics.getWidth() - 220, 60);
				font.draw(spriteBatch, "duration: " + Float.toString(toolpath.duration)+"min", Gdx.graphics.getWidth() - 220, 20);
			}
			spriteBatch.end();
		}
		else
			lastline = -1;
		
		if (Gdx.input.isButtonPressed(1)) {
			if(!isWindowUnderCursor()) {
				int x = Gdx.input.getX();
				int y = Gdx.input.getY();
				Vector3 pos = workspace.intersect(camera.getPickRay(x, y));
				tool.position = pos;
			}
		}


		if(Gdx.input.isTouched(0)) {
			if(Gdx.input.isTouched(2)) {
        		cam_move((float)Gdx.input.getDeltaY() * camera.position.len() * -0.01f);
			}
			else if(Gdx.input.isTouched(1)) {
				cam_rotate();
			}
			else {
				cam_translate();
			}
		}
		
		if(Gdx.input.isButtonPressed(2)) {
			if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
				cam_translate();
			else
				cam_rotate();
		}
		
        ui.act(Math.min(arg0, 1 / 30f));
        ui.draw();
	}

	void cam_rotate() {
		if(isWindowUnderCursor())
			return;
		
		Vector3 right = camera.direction.cpy();
		right.crs(camera.up);
		
		Vector3 up = right.cpy();
		up.crs(camera.direction);
		
		Vector3 point = camera.direction.cpy();
		point.mul(camera.position.len());
		point.add(camera.position);
		
		
		camera.translate(up.mul((float)Gdx.input.getDeltaY() * camera.position.len() * 0.01f));
		camera.translate(right.mul(-(float)Gdx.input.getDeltaX() * camera.position.len() * 0.01f));
		camera.lookAt(point.x, point.y, point.z);

	}
	
	boolean isWindowUnderCursor() {
		int x = Gdx.input.getX();
		int y = Gdx.input.getY();
		Vector2 coord = ui.screenToStageCoordinates(new Vector2(x,y));
		Actor a = ui.hit(coord.x, coord.y, false);
		return a != null;
	}
	
	void cam_translate() {
		if(isWindowUnderCursor())
			return;
		
		Vector3 right = camera.direction.cpy();
		right.crs(camera.up);
		Vector3 tmp = right.cpy();
		tmp.mul(-Gdx.input.getDeltaX());
		camera.translate(tmp);
		
		Vector3 up = right.cpy();
		up.crs(camera.direction);
		tmp = up.cpy();
		tmp.mul(Gdx.input.getDeltaY());
		camera.translate(tmp);
	}

	void cam_move(float f) {
		Vector3 tmp = camera.direction.cpy();
		tmp.mul(f);
		camera.translate(tmp);
	}
	
	@Override
	public void resize(int width, int height) {
		Gdx.graphics.getGL20().glViewport(0, 0, width, height);
		camera.viewportHeight = height;
		camera.viewportWidth = width;
		orthocam.viewportHeight = height;
		orthocam.viewportWidth = width;
		orthocam.position.x = width/2;
		orthocam.position.y = height/2;
        ui.setViewport(width, height, false);
	}

	@Override
	public void resume() {
		
	}
	
	public void showMessage(String text, String caption) {
		Dialog w = new Dialog(caption, skin);
		w.text(text);
		w.button("Ok");
		w.setModal(true);
		w.show(ui);
	}
	@Override
	public void show() {
		spriteBatch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(0, 1, 0, 1);
		
		workspace = new Workspace(-150,150,-200,200,0,50);
		//camera = new OrthographicCamera(800,450);
		camera = new PerspectiveCamera();
		camera.translate(150, 150, 150);
		camera.up.x = 0;
		camera.up.y = 0;
		camera.up.z = 1;
		camera.lookAt(0, 0, 0);
		camera.fieldOfView = 30;
		camera.far = 10000;
		camera.near = 10;
		camera.viewportHeight = Gdx.graphics.getHeight();
		camera.viewportWidth = Gdx.graphics.getWidth();
		
		orthocam = new OrthographicCamera(camera.viewportWidth,camera.viewportHeight);
		
		tool = new Tool();
		current = new Tool();
		

        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false){
        	@Override
        	public boolean scrolled(int amount) {
        		cam_move((float)amount * camera.position.len() * -0.1f);
				return true;
        	}
        };

        ui.addActor(console = new ConsoleWindow(skin, this));
        
        if(device != null) {
			try {
				grbl = GrblStreamFactory.create(device);
				grbl.setListener(new GrblStreamListener(){
					@Override
					public void received(String line) {
						console.queueLine(line);
					}
				});
			}
			catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
        }
        
        ui.addActor(new JogWindow(skin, this));
        ui.addActor(new ViewWindow(skin, this));
        //ui.addActor(new SettingsWindow(skin));
        ui.addActor(new ControlWindow(skin, grbl, this));
	    
		Gdx.input.setInputProcessor(ui);
	}
	
}
