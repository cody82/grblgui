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

public class Main extends Game implements InputProcessor {
	public Main(String _filename, String _device) {
		filename = _filename;
		device = _device;
	}
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
	
	@Override
	public void create() {

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
		
		

		try {
			file = GCodeParser.parseFile(filename);

			grbl = new GrblStream(device, file);
			
			toolpath = Toolpath.fromGCode(file);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		Gdx.input.setInputProcessor(this);
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
	}
	
	float clicktimer;
	
	float postimer;
	Vector3 lastpos = new Vector3(0,0,0);
	
	float speed;
	
	@Override
	public void render() {

		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
		Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);
		//Gdx.gl20.glDisable(GL20.GL_BLEND);
		//Gdx.gl20.glLineWidth(2);
		
		float t = Gdx.graphics.getDeltaTime();
		clicktimer+=t;
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
		
		toolpath.currentLine = grbl.streamer.currentLine;
		
		Matrix4 matrix = camera.combined.cpy();
		workspace.draw(matrix);
		toolpath.draw(matrix);
		tool.draw(matrix);
		current.draw(matrix);

		orthocam.update();
		spriteBatch.setProjectionMatrix(orthocam.projection);
		spriteBatch.setTransformMatrix(orthocam.view);
		spriteBatch.begin();
		int currentline = grbl.streamer.currentLine;
		int maxlines = Gdx.graphics.getHeight() / 20 - 1;
		for(int i = currentline;i > 0 && i > currentline - maxlines;--i) {
			font.draw(spriteBatch, file.gcode.get(i).getContent(), 20, 20 + (currentline - i) * 20);
		}
		font.draw(spriteBatch, "status: " + (grbl.isHold() ? "hold" : "running"), Gdx.graphics.getWidth() - 180, 80);
		font.draw(spriteBatch, "eta:" + Float.toString(toolpath.getEta())+"min", Gdx.graphics.getWidth() - 180, 60);
		font.draw(spriteBatch, "speed: " + Float.toString(speed)+"mm/min", Gdx.graphics.getWidth() - 180, 40);
		font.draw(spriteBatch, "duration: " + Float.toString(toolpath.duration)+"min", Gdx.graphics.getWidth() - 180, 20);
		spriteBatch.end();
		
		/*if(clicktimer >= 0.5f)
		{
			clicktimer = 0;
		for(int i=0;i<5;++i) {
    		if(Gdx.input.isTouched(i)) {
    			int x = Gdx.input.getX(i);
    			int y = Gdx.input.getY(i);
    			Vector3 pos = workspace.intersect(camera.getPickRay(x, y));
    			tool.position = pos;
    			try {
    				String cmd = "G0X" + pos.x + "Y" + pos.y + "Z" + pos.z + "\n";
					grbl.out.write(cmd.getBytes());
					System.out.print(cmd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    		}
		}
		
		}*/
	}
	
	@Override
	public void dispose() {
		grbl.dispose();
	}

	@Override
	public boolean keyDown(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char arg0) {
		if(arg0 == 'p') {
			grbl.pause();
		}
		return false;
	}

	@Override
	public boolean keyUp(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchMoved(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		return false;
	}
}
