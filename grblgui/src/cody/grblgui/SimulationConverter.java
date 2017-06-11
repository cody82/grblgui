package cody.grblgui;

public class SimulationConverter {
	public static SimulationConverter instance;
	
	public static Part convert(Toolpath path, ToolInfo info){
		if(instance != null)
			return instance.convertImpl(path, info);
		else
			return null;
	}
	
	public Part convertImpl(Toolpath path, ToolInfo info){
		Simulation sim = new Simulation(300, 400, 50, 1f);
		sim.simulate(path, info);
		//Tuple2<Object, Object> tmp = sim.getZminmax();
		//float min = (float)tmp._1;
		//float max = (float)tmp._2;

		return new SimulationPart(sim);
	}
}
