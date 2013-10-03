package cody.gcode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class GCodeParser {
	public static GCodeFile parseFile(String filename) throws java.io.IOException{
		String s = readFileAsString(filename);
		return parse(s);
	}
	
	static float feed;
	
	public static GCodeFile parse(String gcode) {
		gcode = gcode.replace(" ", "");
		
		String[] lines = gcode.replace("\r", "").toUpperCase().split("\n");
		int j=0;
		
		GCodeFile file = new GCodeFile();
		
		feed = 0;
		
		for(String line : lines) {
			char cmd = 0;
			String arg = "";
			j++;
			
			GCodeLine gcodeline = new GCodeLine(j, line);
			gcodeline.feedrate = feed;
			file.gcode.add(gcodeline);
			
			for(int i=0;i<line.length();++i) {
				char c = line.charAt(i);
				if(c >= 'A' && c <= 'Z') {
					if(cmd != 0) {
						addCommand(gcodeline, cmd, arg.length() > 0 ? Float.parseFloat(arg) : 0);
					}
					cmd = c;
					arg = "";
				}
				else if((c >= '0' && c <= '9') || c == '.' || c == '-'){
					arg += c;
				}
			}
			if(cmd != 0)
				addCommand(gcodeline, cmd, arg.length() > 0 ? Float.parseFloat(arg) : 0);
			
		}
		return file;
	}
	
	private static void addCommand(GCodeLine line, char cmd, float arg) {
		line.addCommand(cmd, arg);
		if(cmd == 'F') {
			line.feedrate = feed = arg;
		}
	}
	
	private static String readFileAsString(String filePath) throws java.io.IOException{
	    byte[] buffer = new byte[(int) new File(filePath).length()];
	    BufferedInputStream f = null;
	    try {
	        f = new BufferedInputStream(new FileInputStream(filePath));
	        f.read(buffer);
	    } finally {
	        if (f != null) try { f.close(); } catch (IOException ignored) { }
	    }
	    return new String(buffer);
	}
}
