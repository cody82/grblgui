package cody.gcode;

public class GCodeCommand {
	public GCodeCommand(char _cmd, float _arg) {
		cmd = _cmd;
		arg = _arg;
	}
	public char cmd;
	public float arg;
}
