package cody.gcode;

import java.util.ArrayList;

public class GCodeLine {
	String content;
	public GCodeLine(int _line, String original) {
		line = _line;
		content = original;
	}
	
	public String getContent() {
		return content;
	}
	public void addCommand(char cmd, float arg) {
		commands.add(new GCodeCommand(cmd, arg));
	}
	
	public GCodeCommand findCommand(char cmd) {
		for(GCodeCommand c : commands) {
			if(c.cmd == cmd)
				return c;
		}
		return null;
	}
	
	public GCodeCommand getX() {
		return findCommand('X');
	}
	public GCodeCommand getY() {
		return findCommand('Y');
	}
	public GCodeCommand getZ() {
		return findCommand('Z');
	}
	public GCodeCommand getF() {
		return findCommand('F');
	}
	public GCodeCommand getS() {
		return findCommand('S');
	}
	public GCodeCommand getG() {
		return findCommand('G');
	}
	
	public boolean isFeedBeforePosition() {
		return commands.indexOf(getF()) < commands.indexOf(getX());
	}
	public boolean isGBeforePosition() {
		return commands.indexOf(getG()) < commands.indexOf(getX());
	}
	public boolean hasPosition() {
		return getX() != null || getY() != null || getZ() != null;
	}
	public boolean hasFeed() {
		return getF() != null;
	}
	public boolean hasG() {
		return getG() != null;
	}
	public int line;
	public ArrayList<GCodeCommand> commands = new ArrayList<GCodeCommand>();
	public float time;
}
