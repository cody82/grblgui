package cody.grbl;

import cody.gcode.GCodeFile;

public abstract class GrblStreamFactory {
	public static GrblStreamFactory instance;
	
	public static GrblStreamInterface create(String port, int baudrate, GCodeFile file) {
		return instance.createImpl(port, baudrate, file);
	}
	public static GrblStreamInterface create(String port, int baudrate) {
		return instance.createImpl(port, baudrate);
	}

	public static String[] ports() {
		if(instance != null)
			return instance.portsImpl();
		else
			return new String[0];
	}
	
	protected GrblStreamInterface createImpl(String port, int baudrate, GCodeFile file) {
		return null;
	}
	
	protected GrblStreamInterface createImpl(String port, int baudrate) {
		return null;
	}
	
	protected String[] portsImpl() {
		return null;
	}
}
