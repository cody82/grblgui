package cody.grblgui;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class GrblGuiAndroidActivity extends AndroidApplication {
    
	Main main;
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize(main = new Main(null, null), true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.equals(exit)) {
			//maingame.beginMainMenu();
		}
		return true;
		
	}
	MenuItem exit;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		exit = menu.add("Main menu");
		
		return true;
	}
}