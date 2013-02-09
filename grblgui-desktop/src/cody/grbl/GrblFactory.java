package cody.grbl;

import jssc.SerialPortList;
import cody.gcode.GCodeFile;

public class GrblFactory extends GrblStreamFactory {
	@Override
	protected GrblStreamInterface createImpl(String port, GCodeFile file) {
		try {
			return new GrblStream(port,file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(7);
			return null;
		}
	}

	@Override
	protected GrblStreamInterface createImpl(String port) {
		try {
			return new GrblStream(port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(7);
			return null;
		}
	}

	@Override
	protected String[] portsImpl() {
		return SerialPortList.getPortNames();
	}
}
