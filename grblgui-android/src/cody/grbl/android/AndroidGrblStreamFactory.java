package cody.grbl.android;

import com.hoho.android.usbserial.driver.UsbSerialDriver;

import android.hardware.usb.UsbManager;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import cody.gcode.GCodeFile;
import cody.grbl.GrblStreamFactory;
import cody.grbl.GrblStreamInterface;

public class AndroidGrblStreamFactory extends GrblStreamFactory {
	UsbManager manager;
	public AndroidGrblStreamFactory(UsbManager _manager) {
		manager = _manager;
	}
	@Override
	protected GrblStreamInterface createImpl(String port, int baudrate, GCodeFile file) {
		GrblStreamInterface s = createImpl(port, baudrate);
		s.stream(file);
		return s;
	}

	@Override
	protected GrblStreamInterface createImpl(String port, int baudrate) {
		// Find the first available driver.
		UsbSerialDriver driver = UsbSerialProber.acquire(manager);
		if(driver != null)
			try {
				return new AndroidGrblStream(driver);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(85);
				return null;
			}
		else
			return null;
	}

	@Override
	protected String[] portsImpl() {
		return new String[] {"First port"};
	}
}
