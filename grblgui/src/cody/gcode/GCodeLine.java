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
	
	public String getContent(int speed) {
		if(speed == 100)
			return getContent();
		
		GCodeCommand cmd = getF();
		if(cmd == null)
			return getContent();
		
		String ret = "";
		
		for(GCodeCommand c : commands) {
			if(c.cmd == 'F'){
				ret += Character.toString(c.cmd) + Integer.toString(speed * (int)c.arg / 100);
			}
			else {
				ret += Character.toString(c.cmd) + Integer.toString((int)c.arg);
			}
		}
		
		return ret;
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
	public GCodeCommand getI() {
		return findCommand('I');
	}
	public GCodeCommand getJ() {
		return findCommand('J');
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
	public boolean hasG01() {
		GCodeCommand g = getG();
		if(g == null)
			return false;
		
		if(((int)g.arg+0.5f) == 0 || ((int)g.arg+0.5f) == 1)
			return true;
		else
			return false;
	}
	public boolean hasG2() {
		GCodeCommand g = getG();
		if(g == null)
			return false;
		if(((int)(g.arg+0.5f)) == 2)
			return true;
		else
			return false;
	}
	public boolean hasG3() {
		GCodeCommand g = getG();
		if(g == null)
			return false;
		if(((int)(g.arg+0.5f)) == 3)
			return true;
		else
			return false;
	}
	public int line;
	public ArrayList<GCodeCommand> commands = new ArrayList<GCodeCommand>();
	public float time;
	public float feedrate;
}
