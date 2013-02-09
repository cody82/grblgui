package cody.grbl;

import cody.gcode.GCodeFile;

public abstract class GrblStreamFactory {
	public static GrblStreamFactory instance;
	
	public static GrblStreamInterface create(String port, GCodeFile file) {
		return instance.createImpl(port, file);
	}
	public static GrblStreamInterface create(String port) {
		return instance.createImpl(port);
	}

	public static String[] ports() {
		if(instance != null)
			return instance.portsImpl();
		else
			return new String[0];
	}
	
	protected GrblStreamInterface createImpl(String port, GCodeFile file) {
		return null;
	}
	
	protected GrblStreamInterface createImpl(String port) {
		return null;
	}
	
	protected String[] portsImpl() {
		return null;
	}
}
