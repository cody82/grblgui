package cody.grbl;

import com.badlogic.gdx.math.Vector3;

import cody.gcode.GCodeFile;


public interface GrblStreamInterface {

	boolean isStreaming();

	void stopStream();

	void stream(GCodeFile file);

	void pause();

	boolean isHold();

	void send(byte[] bs);

	void dispose();

	Vector3 getToolPosition();
	
	Vector3 getMachinePosition();

	int getCurrentLine();
	
	void setListener(GrblStreamListener listener);
	
	void feedOverride(GrblFeedOverride percent);
	
	void jogCancel();
	
	int getSpeed();
	String getStatus();
}
