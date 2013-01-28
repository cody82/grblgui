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
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class ControlWindow extends Window{

	GrblStream grbl;
	GCodeFile file;
	MainScreen mainscreen;
	
	String[] getFiles() {
		return new String[] {"fgdfng", "fgjnfgj"};
	}
	
	public ControlWindow(Skin skin, GrblStream _grbl, MainScreen _mainscreen) {
		super("Control", skin);
		grbl = _grbl;
		setBounds(600, 0, 150, 250);
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

        preview_button.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            			if(mainscreen.toolsize > 0) {
            				Simulation sim = new Simulation(300, 300, 50, 1f);
            				sim.simulate(mainscreen.toolpath, new ToolInfo(mainscreen.toolsize));
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
            			
            				mainscreen.part = new Part(sim);
            				mainscreen.draw_part = true;
            			}
            			
            			
    				return true;
            	}});
        

        add(file_select).fill().expand();
        row();
        add(load_button).fill().expand();
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
