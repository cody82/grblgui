package cody.grblgui;

import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public class SettingsWindow extends Window {

	public SettingsWindow(Skin skin) {
		super("Settings", skin);
		
		setBounds(500, 0, 700, 300);
		setColor(0, 0, 1, 0.8f);

		Table table1 = new Table();
		add(table1).expand().fill();

		table1.add(new Label("X-steps/mm", skin)).fill().expand();
		table1.add(new TextField("400.000", skin)).width(80).left();
		table1.add(new Label("X-max[mm/s]", skin)).fill().expand();
		table1.add(new TextField("400.000", skin)).width(80).left();
		table1.add(new Label("X-acc[mm/s^2]", skin)).fill().expand();
		table1.add(new TextField("50.000", skin)).width(80).left();
		table1.row();
		table1.add(new Label("Y-steps/mm", skin)).fill().expand();
		table1.add(new TextField("400.000", skin)).width(80).left();
		table1.add(new Label("Y-max[mm/s]", skin)).fill().expand();
		table1.add(new TextField("400.000", skin)).width(80).left();
		table1.add(new Label("Y-acc[mm/s^2]", skin)).fill().expand();
		table1.add(new TextField("50.000", skin)).width(80).left();
		table1.row();
		table1.add(new Label("Z-steps/mm", skin)).fill().expand();
		table1.add(new TextField("400.000", skin)).width(80).left();
		table1.add(new Label("Z-max[mm/s]", skin)).fill().expand();
		table1.add(new TextField("400.000", skin)).width(80).left();
		table1.add(new Label("Z-acc[mm/s^2]", skin)).fill().expand();
		table1.add(new TextField("50.000", skin)).width(80).left();
		
		row();
		
		Table table2 = new Table();
		add(table2).expand().fill();
		table2.add(new CheckBox("X-Step", skin)).fill().expand();
		table2.add(new CheckBox("X-Dir", skin)).fill().expand();
		table2.add(new CheckBox("X-Home", skin)).fill().expand();
		table2.row();
		table2.add(new CheckBox("Y-Step", skin)).fill().expand();
		table2.add(new CheckBox("Y-Dir", skin)).fill().expand();
		table2.add(new CheckBox("Y-Home", skin)).fill().expand();
		table2.row();
		table2.add(new CheckBox("Z-Step", skin)).fill().expand();
		table2.add(new CheckBox("Z-Dir", skin)).fill().expand();
		table2.add(new CheckBox("Z-Home", skin)).fill().expand();


	}

}
