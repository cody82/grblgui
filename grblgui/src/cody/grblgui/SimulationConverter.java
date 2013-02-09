package cody.grblgui;

public abstract class SimulationConverter {
	public static SimulationConverter instance;
	
	public static Part convert(Toolpath path, ToolInfo info){
		if(instance != null)
			return instance.convertImpl(path, info);
		else
			return null;
	}
	
	public Part convertImpl(Toolpath path, ToolInfo info){
		return null;
	}
}
