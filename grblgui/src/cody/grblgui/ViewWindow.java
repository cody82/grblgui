package cody.grblgui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class ViewWindow extends Window {
	
	MainScreen mainscreen;
	CheckBox draw_part;
	CheckBox show_console;
	CheckBox camera_follow;
	
	public ViewWindow(Skin skin, MainScreen _mainscreen) {
		super("View", skin);
		
		setBounds(0, 400, 200, 200);
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
		show_console.setChecked(true);
		show_console.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		mainscreen.console.setVisible(!show_console.isChecked());
    				return true;
            	}});
		add(show_console).fill().expand();
		row();
		
		final TextField h = new TextField("400", skin);
		final TextField w = new TextField("300", skin);
		TextButton workspace_size = new TextButton("Set workspace size [mm]", skin);
		workspace_size.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
            			mainscreen.workspace.setSize(Float.parseFloat(w.getText()), Float.parseFloat(h.getText()));
    				return true;
            	}});
		add(workspace_size);
		row();

        Table t = new Table();
        t.add(w).fill().expand();
        t.add(h).fill().expand();
        add(t).fill().expand();
		row();
		
		add(new Label("Camera", skin)).fill().expand();
		row();

		camera_follow = new CheckBox("Follow", skin);
		camera_follow.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		mainscreen.camera_follow = !camera_follow.isChecked();
    				return true;
            	}});
		add(camera_follow).fill().expand();
	}

}
