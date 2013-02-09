package cody.grblgui;

import scala.Tuple2;
import cody.grblgui.Part;
import cody.grblgui.SimulationConverter;
import cody.grblgui.ToolInfo;
import cody.grblgui.Toolpath;

public class ScalaSimulationConverter extends SimulationConverter {
	@Override
	public Part convertImpl(Toolpath path, ToolInfo info){
		Simulation sim = new Simulation(300, 400, 50, 1f);
		sim.simulate(path, info);
		//Tuple2<Object, Object> tmp = sim.getZminmax();
		//float min = (float)tmp._1;
		//float max = (float)tmp._2;

		return new SimulationPart(sim);
	}
}
