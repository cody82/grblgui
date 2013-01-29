package cody.grblgui;

import java.io.IOException;

import scala.Tuple2;

import cody.gcode.GCodeFile;
import cody.gcode.GCodeParser;
import cody.grbl.GrblStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class ControlWindow extends Window{

	GrblStream grbl;
	GCodeFile file;
	MainScreen mainscreen;
	TextField tool_radius;
	
	String[] getFiles() {
		return new String[] {"fgdfng", "fgjnfgj"};
	}
	
	public ControlWindow(Skin skin, GrblStream _grbl, MainScreen _mainscreen) {
		super("Control", skin);
		grbl = _grbl;
		setBounds(600, 0, 250, 250);
		setColor(0.5f, 0.5f, 0.5f, 0.8f);
		String dir_or_file = _mainscreen.filename;
		mainscreen = _mainscreen;
		
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

        final TextButton exit_button = new TextButton("Quit", skin);

        exit_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		Gdx.app.exit();
    				return true;
            	}});

        FileHandle fh = Gdx.files.absolute(dir_or_file);
        FileHandle[] files;
        boolean isDirectory = fh.isDirectory();
        if(isDirectory) {
        	files = fh.list();
        	for(FileHandle f: files) {
        	}
        } else {
        	files = new FileHandle[] { fh };
        }
        final FileHandle[] filesfinal = files;
        final SelectBox file_select = new SelectBox(files, skin);
        final TextButton load_button = new TextButton("Load", skin);

        load_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            			FileHandle f = filesfinal[file_select.getSelectionIndex()];
            			

            			try {
            				file = GCodeParser.parseFile(f.path());
            				mainscreen.file = file;
            				mainscreen.toolpath = Toolpath.fromGCode(file);
            			} catch (IOException e) {
            				e.printStackTrace();
            				System.exit(1);
            			}
            			catch (Exception e) {
            				e.printStackTrace();
            				System.exit(1);
            			}
            			
            			
    				return true;
            	}});

        final TextButton preview_button = new TextButton("Generate preview", skin);

        tool_radius = new TextField("5",skin);
        preview_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            				Simulation sim = new Simulation(300, 300, 50, 1f);
            				mainscreen.toolsize = Float.parseFloat(tool_radius.getText());
            				sim.simulate(mainscreen.toolpath, new ToolInfo(mainscreen.toolsize));
            				Tuple2<Object, Object> tmp = sim.getZminmax();
            				float min = (float)tmp._1;
            				float max = (float)tmp._2;
            		
            				System.out.println("min: " + min + " max: " + max);

            				mainscreen.part = new Part(sim);
            				mainscreen.draw_part = true;
            			
            			
    				return true;
            	}});

        add(file_select).fill().expand();
        row();
        add(load_button).fill().expand();
        row();
        Table t = new Table();
        t.add(new Label("Tool radius[mm]", skin)).fill().expand();
        t.add(tool_radius).width(50);
        add(t).fill().expand();
        row();
        add(preview_button).fill().expand();
        row();
        add(stream_button).fill().expand();
        row();
        add(hold_button).fill().expand();
        row();
        add(exit_button).fill().expand();
        
	}

}
