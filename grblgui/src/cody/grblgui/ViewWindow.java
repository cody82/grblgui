package cody.grblgui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class ViewWindow extends Window {
	
	MainScreen mainscreen;
	CheckBox draw_part;
	CheckBox show_console;
	
	public ViewWindow(Skin skin, MainScreen _mainscreen) {
		super("View", skin);
		
		setBounds(300, 0, 200, 200);
		setColor(0, 1, 0, 0.8f);
		
		mainscreen = _mainscreen;
		
		draw_part = new CheckBox("Draw preview", skin);
		draw_part.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		mainscreen.draw_part = !draw_part.isChecked();
    				return true;
            	}});
		add(draw_part).fill().expand();

		row();
		final CheckBox ztest = new CheckBox("Z-Test", skin);
		ztest.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		mainscreen.ztest = !ztest.isChecked();
    				return true;
            	}});
		add(ztest).fill().expand();
		
		row();
		
		show_console = new CheckBox("Show console", skin);
		show_console.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		mainscreen.console.setVisible(!show_console.isChecked());
    				return true;
            	}});
		add(show_console).fill().expand();
		row();
		
		add(new Label("Camera", skin)).fill().expand();
	}

}
