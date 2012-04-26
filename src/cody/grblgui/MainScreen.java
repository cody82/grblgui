package cody.grblgui;

import java.io.IOException;

import cody.gcode.GCodeFile;
import cody.gcode.GCodeParser;
import cody.grbl.GrblStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;

public class MainScreen implements Screen {

	GrblStream grbl;
	
	Workspace workspace;
	PerspectiveCamera camera;
	OrthographicCamera orthocam;
	Tool tool;
	Tool current;
	Toolpath toolpath;
	
	SpriteBatch spriteBatch;
	BitmapFont font;
	GCodeFile file;
	String filename;
	String device;


    Skin skin;
    Stage ui;
	Window window;
	
	float postimer;
	Vector3 lastpos = new Vector3(0,0,0);
	
	float speed;
	
	public MainScreen(String _filename, String _device) {
		filename = _filename;
		device = _device;
	}
	
	@Override
	public void dispose() {
		grbl.dispose();
	}

	@Override
	public void hide() {
		grbl.dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void render(float arg0) {
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);
		//Gdx.gl20.glDisable(GL20.GL_BLEND);
		//Gdx.gl20.glLineWidth(2);
		
		float t = arg0;
		postimer+=t;

		Vector3 tooltargetpos = grbl.toolPosition.cpy();
		Vector3 d = tooltargetpos.sub(current.position);
		float l = d.len();
		Vector3 result = current.position.add(d.mul(Math.min(t * 10f, 1f)));
		current.position = result;
		//camera.rotate(1, 0, 1, 1);
		camera.lookAt(current.position.x, current.position.y, current.position.z);
		camera.update(true);
		

		if(postimer >= 1f) {
			speed = current.position.dst(lastpos) / postimer * 60f;
			lastpos = current.position.cpy();
			postimer = 0;
		}

		int currentline = toolpath.currentLine = grbl.streamer != null ? grbl.streamer.currentLine : -1;
		
		Matrix4 matrix = camera.combined.cpy();
		workspace.draw(matrix);
		toolpath.draw(matrix);
		tool.draw(matrix);
		current.draw(matrix);

		orthocam.update();
		spriteBatch.setProjectionMatrix(orthocam.projection);
		spriteBatch.setTransformMatrix(orthocam.view);
		spriteBatch.begin();
		int maxlines = Gdx.graphics.getHeight() / 20 - 1;
		for(int i = currentline;i > 0 && i > currentline - maxlines;--i) {
			if(i<file.gcode.size())
				font.draw(spriteBatch, file.gcode.get(i).getContent(), 20, 20 + (currentline - i) * 20);
		}
		font.draw(spriteBatch, "position: X" + grbl.toolPosition.x + "Y" + grbl.toolPosition.y + "Z" +grbl.toolPosition.z, Gdx.graphics.getWidth() - 220, 100);
		font.draw(spriteBatch, "status: " + (grbl.isStreaming() ? "streaming " : "") + (grbl.isHold() ? "hold" : "running"), Gdx.graphics.getWidth() - 220, 80);
		font.draw(spriteBatch, "speed: " + Float.toString(speed)+"mm/min", Gdx.graphics.getWidth() - 220, 40);
		if(grbl.isStreaming()) {
			font.draw(spriteBatch, "eta:" + Float.toString(toolpath.getEta())+"min", Gdx.graphics.getWidth() - 220, 60);
			font.draw(spriteBatch, "duration: " + Float.toString(toolpath.duration)+"min", Gdx.graphics.getWidth() - 220, 20);
		}
		spriteBatch.end();
		
		if (Gdx.input.isButtonPressed(1)) {
			int x = Gdx.input.getX();
			int y = Gdx.input.getY();
			Vector3 pos = workspace.intersect(camera.getPickRay(x, y));
			tool.position = pos;
			if (!grbl.isStreaming()) {
				String cmd = "G0X" + pos.x + "Y" + pos.y + "Z" + pos.z;
				cmd_field.setText(cmd);
			}
		}
		
		
		if(Gdx.input.isButtonPressed(2)) {
			camera.position.x += Gdx.input.getDeltaX();
			camera.position.y += Gdx.input.getDeltaY();
		}
		
        ui.act(Math.min(arg0, 1 / 30f));
        ui.draw();
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
		// TODO Auto-generated method stub
		
	}
	TextField cmd_field;
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
		

        skin = new Skin(Gdx.files.internal("uiskin.json"), Gdx.files.internal("uiskin.png"));
        ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false){
        	@Override
        	public boolean scrolled(int amount) {
        		camera.position.z += amount * 10;
				return super.scrolled(amount);
        	}
        };
        window = new Window("Controls", skin.getStyle(WindowStyle.class));
        window.color.a = 0.5f;
        window.height = Gdx.graphics.getHeight() / 2;
        window.width = Gdx.graphics.getWidth() / 4;
        window.x = Gdx.graphics.getWidth() - window.width;
        window.y = Gdx.graphics.getHeight() - window.height;
        
        final TextField file_field = new TextField(filename, skin.getStyle(TextFieldStyle.class));
        window.add(file_field).fill(0f, 0f);
        window.row();
        
        final TextButton load_button = new TextButton("Load", skin.getStyle(TextButtonStyle.class), "button-sl") {
        	@Override
        	public boolean touchDown(float x, float y, int pointer) {
    			if(!grbl.isStreaming()) {
    				filename = file_field.getText();
    				try {
    					file = GCodeParser.parseFile(filename);
    					
    					toolpath = Toolpath.fromGCode(file);
    				} catch (IOException e) {
    					e.printStackTrace();
    					System.exit(1);
    				}
    				catch (Exception e) {
    					e.printStackTrace();
    					System.exit(1);
    				}
    			}
				return isChecked();
        	}
        };
        window.add(load_button).fill(0f, 0f);
        window.row();
        
        final TextButton stream_button = new TextButton("Start streaming", skin.getStyle(TextButtonStyle.class), "button-sl") {
        	@Override
        	public boolean touchDown(float x, float y, int pointer) {
    			if(grbl.isStreaming()) {
    				grbl.stopStream();
    				setText("Start streaming");
    				color.r = 1;
    				color.g = 0;
    				color.b = 0;
    			}
    			else {
    				grbl.stream(file);
    				setText("Stop streaming");
    				color.r = 0;
    				color.g = 1;
    				color.b = 0;
    			}
				return isChecked();
        	}
        };
        stream_button.color.r = 1;
        stream_button.color.g = 0;
        stream_button.color.b = 0;
        
        final TextButton hold_button = new TextButton("Enable feed hold", skin.getStyle(TextButtonStyle.class), "button-sl") {
        	@Override
        	public boolean touchDown(float x, float y, int pointer) {
    			grbl.pause();
    			if(grbl.isHold()) {
    				setText("Disable feed hold");
    				color.r = 1;
    				color.g = 0;
    				color.b = 0;
    			}
    			else {
    				setText("Enable feed hold");
    				color.r = 0;
    				color.g = 1;
    				color.b = 0;
    			}
				return isChecked();
        	}
        };
        hold_button.color.r = 0;
        hold_button.color.g = 1;
        hold_button.color.b = 0;
        
        window.add(stream_button).fill(0f, 0f);
        window.row();
        window.add(hold_button).fill(0f, 0f);
        
        cmd_field = new TextField("", skin.getStyle(TextFieldStyle.class));
        final TextButton cmd_button = new TextButton("Execute", skin.getStyle(TextButtonStyle.class), "button-sl") {
        	@Override
        	public boolean touchDown(float x, float y, int pointer) {
        		if(!grbl.isStreaming())
        			grbl.send((cmd_field.getText() + "\n").getBytes());
				return isChecked();
        	}
        };
        window.row();
        window.add(cmd_field).fill(0f, 0f);
        window.row();
        window.add(cmd_button).fill(0f, 0f);
        
        final TextButton exit_button = new TextButton("Quit", skin.getStyle(TextButtonStyle.class), "button-sl") {
        	@Override
        	public boolean touchDown(float x, float y, int pointer) {
        		Gdx.app.exit();
				return isChecked();
        	}
        };
        window.row();
        window.add(exit_button).fill(0f, 0f);
        
        ui.addActor(window);
        
		try {
			file = GCodeParser.parseFile(filename);

			grbl = new GrblStream(device);
			
			toolpath = Toolpath.fromGCode(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		Gdx.input.setInputProcessor(ui);
	}
	
}
