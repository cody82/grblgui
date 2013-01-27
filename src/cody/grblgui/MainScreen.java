package cody.grblgui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import scala.Tuple2;

import cody.gcode.GCodeFile;
import cody.gcode.GCodeParser;
import cody.grbl.GrblStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class MainScreen implements Screen {

	GrblStream grbl;
	
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
	String filename;
	String device;


    Skin skin;
    Stage ui;
	Window window;
	
	float postimer;
	Vector3 lastpos = new Vector3(0,0,0);
	
	float speed;
	float toolsize;
	
	public MainScreen(String _filename, String _device, float _toolsize) {
		filename = _filename;
		device = _device;
		toolsize = _toolsize;
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

	boolean ztest = true;
	
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

		Vector3 tooltargetpos = grbl.toolPosition.cpy();
		Vector3 d = tooltargetpos.sub(current.position);

		Vector3 result = current.position.add(d.mul(Math.min(t * 10f, 1f)));
		current.position = result;
		//camera.rotate(1, 0, 1, 1);
		//camera.lookAt(current.position.x, current.position.y, current.position.z);
		camera.update(true);
		

		if(postimer >= 1f) {
			speed = current.position.dst(lastpos) / postimer * 60f;
			lastpos = current.position.cpy();
			postimer = 0;
		}

		int currentline = toolpath.currentLine = grbl.streamer != null ? grbl.streamer.currentLine : -1;
		
		Matrix4 matrix = camera.combined.cpy();
		if(part != null && draw_part)
			part.draw(matrix);
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
			if(Gdx.input.isKeyPressed(Keys.CONTROL_LEFT))
				cam_translate();
			else
				cam_rotate();
			/*Matrix4 m2 = camera.view.cpy();
			m2.inv();
			Matrix4 m = new Matrix4();
			m.idt();
			m.translate(Gdx.input.getDeltaX(), Gdx.input.getDeltaY(), Gdx.input.getDeltaX());
			m2.mul(m);
			m2.inv();
			m2.getTranslation(camera.position);

			//camera.update();
			//m2.getTranslation(camera.position);
			//tmp.mul();
			//camera.position.add(tmp);
			
			/*tmp = camera.view
			tmp.mul(Gdx.input.getDeltaX());
			camera.position.add(tmp);
			camera.position.x += Gdx.input.getDeltaX();
			camera.position.y += ;*/
		}
		
        ui.act(Math.min(arg0, 1 / 30f));
        ui.draw();
	}

	/*void cam_rotate() {
		Vector3 right = camera.direction.cpy();
		right.crs(camera.up);
		
		Vector3 up = right.cpy();
		up.crs(camera.direction);
		
		camera.rotate(Gdx.input.getDeltaY(), right.x,right.y,right.z);
		camera.rotate(Gdx.input.getDeltaX(), up.x,up.y,up.z);
	}*/

	void cam_rotate() {
		Vector3 right = camera.direction.cpy();
		right.crs(camera.up);
		
		Vector3 up = right.cpy();
		up.crs(camera.direction);
		
		Vector3 point = camera.direction.cpy();
		point.mul(camera.position.len());
		point.add(camera.position);
		
		
		camera.translate(up.mul((float)Gdx.input.getDeltaY() * camera.position.len() * 0.01f));
		camera.translate(right.mul(-(float)Gdx.input.getDeltaX() * camera.position.len() * 0.01f));
		//camera.rotateAround(point, up, Gdx.input.getDeltaX());
		//camera.rotateAround(point, right, Gdx.input.getDeltaY());
		//camera.rotate(Gdx.input.getDeltaY(), right.x,right.y,right.z);
		//camera.rotate(Gdx.input.getDeltaX(), up.x,up.y,up.z);
		camera.lookAt(point.x, point.y, point.z);

	}
	
	void cam_translate() {
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
		

        skin = new Skin(Gdx.files.internal("data/uiskin.json"));
        ui = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false){
        	@Override
        	public boolean scrolled(int amount) {
        		cam_move((float)amount * camera.position.len() * -0.1f);
				return true;
        	}
        };
        window = new Window("Controls", skin);
        window.setColor(0,0,0,0.5f);
        window.setBounds(Gdx.graphics.getWidth() - Gdx.graphics.getHeight() / 2, Gdx.graphics.getHeight() - Gdx.graphics.getWidth() / 4, Gdx.graphics.getWidth() / 4, Gdx.graphics.getHeight() / 2);

        
        final TextField file_field = new TextField(filename, skin);
        window.add(file_field).fill(0f, 0f);
        window.row();
        
        final TextButton load_button = new TextButton("Load", skin);
        load_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
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
    				return true;
            	}});
        
        window.add(load_button).fill(0f, 0f);
        window.row();
        
        final TextButton stream_button = new TextButton("Start streaming", skin);
        
        stream_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            			if(grbl.isStreaming()) {
            				grbl.stopStream();
            				stream_button.setText("Start streaming");
            				stream_button.setColor(1, 0, 0, 1);
            			}
            			else {
            				grbl.stream(file);
            				stream_button.setText("Stop streaming");
            				stream_button.setColor(0, 1, 0, 1);
            			}
    				return true;
            	}});

		stream_button.setColor(1, 0, 0, 1);
        
        final TextButton hold_button = new TextButton("Enable feed hold", skin);
        
        hold_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            			grbl.pause();
            			if(grbl.isHold()) {
            				hold_button.setText("Disable feed hold");
            				hold_button.setColor(1, 0, 0, 1);
            			}
            			else {
            				hold_button.setText("Enable feed hold");
            				hold_button.setColor(0, 1, 0, 1);
            			}
    				return true;
            	}});
        

		hold_button.setColor(0, 1, 0, 1);
        
        window.add(stream_button).fill(0f, 0f);
        window.row();
        window.add(hold_button).fill(0f, 0f);
        
        cmd_field = new TextField("", skin);
        final TextButton cmd_button = new TextButton("Execute", skin);
        
        cmd_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		if(!grbl.isStreaming())
                			grbl.send((cmd_field.getText() + "\n").getBytes());
    				return true;
            	}});
        
        
        
        
        window.row();
        window.add(cmd_field).fill(0f, 0f);
        window.row();
        window.add(cmd_button).fill(0f, 0f);
        
        final TextButton exit_button = new TextButton("Quit", skin);

        exit_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		Gdx.app.exit();
    				return true;
            	}});
        
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

        ui.addActor(new JogWindow(skin, grbl));
        ui.addActor(new ViewWindow(skin, this));
        
		if(toolsize > 0) {
			Simulation sim = new Simulation(300, 300, 50, 1f);
			sim.simulate(toolpath, new ToolInfo(toolsize));
			Tuple2<Object, Object> tmp = sim.getZminmax();
			float min = (float)tmp._1;
			float max = (float)tmp._2;
	
			System.out.println("min: " + min + " max: " + max);
			/*System.out.println("img");
			BufferedImage img = new BufferedImage(sim.count_x(), sim.count_y(), BufferedImage.TYPE_INT_ARGB);
			for(int y = 0;y<sim.count_y();++y) {
				for(int x = 0;x<sim.count_x();++x) {
					float z = sim.getZ(x, y);
					int color = (int)(((z - min) / (max - min)) * 255f);
					img.setRGB(x, y, color + color << 8 + color << 16);
				}
			}
	
			System.out.println("img2");
			
		    File outputfile = new File("saved.png");
		    try {
				ImageIO.write(img, "png", outputfile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		    */
		
			part = new Part(sim);
			draw_part = true;
		}
		
	    
		Gdx.input.setInputProcessor(ui);
	}
	
}
