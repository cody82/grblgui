package cody.grblgui;

import cody.grbl.GrblConnection;
import cody.grbl.GrblStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class JogWindow extends Window {
	GrblStream grbl;
	
	TextField x;
	TextField y;
	TextField z;
	TextField step;
	CheckBox current;
	TextButton set;
	TextButton zero;
	
	@Override
	public void draw(SpriteBatch arg0, float arg1) {
		Vector3 pos = grbl.toolPosition;
		set.setVisible(!current.isChecked());
		if(current.isChecked()) {
		String tmp = Float.toString(pos.x);
		if(!tmp.equals(x.getText()))
			x.setText(tmp);
		tmp = Float.toString(pos.y);
		if(!tmp.equals(y.getText()))
			y.setText(tmp);
		tmp = Float.toString(pos.z);
		if(!tmp.equals(z.getText()))
			z.setText(tmp);
		}
		super.draw(arg0, arg1);
	}
	
	float getStep() {
		return Float.parseFloat(step.getText());
	}
	float getXpos() {
		return Float.parseFloat(x.getText());
	}
	float getYpos() {
		return Float.parseFloat(y.getText());
	}
	float getZpos() {
		return Float.parseFloat(z.getText());
	}
	void jog(float x, float y, float z) {
		if(grbl.isStreaming())
			return;
		float step = getStep();
		Vector3 v = grbl.toolPosition.cpy();
		v.add(x * step, y * step, z * step);
		grbl.send(("G0X" + Float.toString(v.x) + "Y" + Float.toString(v.y) + "Z" + Float.toString(v.z) + "\n").getBytes());
		//grbl.send(("G1" + "\n").getBytes());
		
	}
	

	void go() {
		if(grbl.isStreaming())
			return;
		grbl.send(("G0X" + getXpos() + "Y" + getYpos() + "Z" + getZpos() + "\n").getBytes());
		//grbl.send(("G1" + "\n").getBytes());
		
	}
	
	public JogWindow(Skin skin, GrblStream _grbl) {
		super("Jog", skin);
		grbl = _grbl;
		
		setBounds(0, 0, 200, 400);
		setColor(1, 0, 0, 0.8f);

		Table table3 = new Table();
		add(table3).expand().fill();
		row();
		
		current = new CheckBox("Current", skin);
		current.setChecked(true);
		table3.add(current).fill().expand();
		set = new TextButton("Move", skin);
		table3.add(set).fill().expand();
		set.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		go();
    				return true;
            	}});
		zero = new TextButton("Set Zero", skin);
		zero.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		if(!grbl.isStreaming()) {
                			//Vector3 v = grbl.machinePosition.cpy().sub(grbl.toolPosition.cpy());
                			Vector3 v = grbl.machinePosition.cpy();
                			grbl.send(("G10 L2 P1 X" + v.x + " Y" + v.y +" Z" + v.z + "\n").getBytes());
                		}
    				return true;
            	}});
		table3.add(zero).fill().expand();
		
		Table table1 = new Table();
		add(table1).expand().fill();
		row();
		
		table1.add(new Label("step", skin)).fill().expand();
		table1.add(step = new TextField("1.000", skin)).fill().expand();
		table1.row();
		table1.add(new Label("X", skin)).fill().expand();
		table1.add(x = new TextField("0.000", skin)).fill().expand();
		table1.row();
		table1.add(new Label("Y", skin)).fill().expand();
		table1.add(y = new TextField("0.000", skin)).fill().expand();
		table1.row();
		table1.add(new Label("Z", skin)).fill().expand();
		table1.add(z = new TextField("0.000", skin)).fill().expand();

		Table table2 = new Table();
		add(table2).expand().fill();
		row();
		
		final TextButton up = new TextButton("Z+", skin);
		table2.add(up).fill().expand();
		up.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		jog(0,0,1);
    				return true;
            	}});
		
		final TextButton yp = new TextButton("Y+", skin);
		table2.add(yp).fill().expand();
		yp.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		jog(0,1,0);
    				return true;
            	}});
		
		table2.add(new Label("", skin)).fill().expand();
		table2.row();
		
		final TextButton xp = new TextButton("X+", skin);
		table2.add(xp).fill().expand();
		xp.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		jog(1,0,0);
    				return true;
            	}});
		
		table2.add(new Label("", skin)).fill().expand();
		
		final TextButton xm = new TextButton("X-", skin);
		table2.add(xm).fill().expand();
		xm.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		jog(-1,0,0);
    				return true;
            	}});
		
		table2.row();
		
		final TextButton zm = new TextButton("Z-", skin);
		table2.add(zm).fill().expand();
		zm.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		jog(0,0,-1);
    				return true;
            	}});
		
		final TextButton ym = new TextButton("Y-", skin);
		table2.add(ym).fill().expand();
		ym.addListener(
            	new InputListener() {
            		@Override
            	public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                		jog(0,-1,0);
    				return true;
            	}});
	}

}
