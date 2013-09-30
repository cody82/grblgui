package cody.grblgui;

import java.util.LinkedList;
import java.util.Queue;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class ConsoleWindow extends Window {
	
	MainScreen mainscreen;
	Label console;
	Queue<String> lines;
	TextField cmd;
	TextButton execute;
	boolean update;
	
	public ConsoleWindow(Skin skin, MainScreen _mainscreen) {
		super("Console", skin);
		
		setBounds(300, 0, 400, 600);
		setColor(0.3f, 0.5f, 0, 0.8f);
		
		mainscreen = _mainscreen;

		console = new Label("", skin);
		add(console).fill().expand();
		row();
		cmd = new TextField("", skin);
		add(cmd).height(30).fill();
		execute = new TextButton("Send",skin);
		add(execute).height(30);

		cmd.addListener(new InputListener(){
			@Override
			public boolean keyUp(InputEvent event, int character) {
				if(character == 66) {

        			if(mainscreen.grbl == null) {
        				mainscreen.showMessage("Open a serial port first.", "Error");
        				return true;
        			}
        			
        			String command = cmd.getText();
        			if(command.isEmpty())
        				return true;
        			
        			cmd.setText("");
        			writeLine(command);
        			
        			mainscreen.grbl.send((command + "\n").getBytes());
				}
					
				return true;
			}
		});

		execute.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            			if(mainscreen.grbl == null) {
            				mainscreen.showMessage("Open a serial port first.", "Error");
            				return true;
            			}
            			String command = cmd.getText();
            			if(command.isEmpty())
            				return true;
            			
            			cmd.setText("");
            			writeLine(command);
            			
            			mainscreen.grbl.send((command + "\n").getBytes());
    				return true;
            	}});
		
		
		
		lines = new LinkedList<String>();
	}
	
	@Override
	public synchronized void draw(SpriteBatch batch, float parentAlpha) {
		if(update) {
			String tmp = "";
			for(String s : lines)
				tmp += s + "\n";
			while(lines.size() > 0 && lines.size() > console.getHeight() / 20 - 1)
				lines.remove();
			console.setText(tmp);
			
			update = false;
		}
		super.draw(batch, parentAlpha);
	}
	
	public synchronized void queueLine(String line) {
		lines.add(line);
		while(lines.size() > 0 && lines.size() > console.getHeight() / 20 - 1)
			lines.remove();
		update = true;
	}
	public synchronized void writeLine(String line) {
		lines.add(line);
		String tmp = "";
		for(String s : lines)
			tmp += s + "\n";
		while(lines.size() > 0 && lines.size() > console.getHeight() / 20 - 1)
			lines.remove();
		console.setText(tmp);
	}
}
