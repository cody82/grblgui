package cody.grblgui;

import cody.grbl.GrblStreamFactory;
import cody.grbl.android.AndroidGrblStreamFactory;

import com.badlogic.gdx.backends.android.AndroidApplication;

import android.content.Context;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class GrblGuiAndroidActivity extends AndroidApplication {
    
	Main main;
	@Override
	public void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);

		GrblStreamFactory.instance = new AndroidGrblStreamFactory(manager);
		
		initialize(main = new Main(null, null));
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