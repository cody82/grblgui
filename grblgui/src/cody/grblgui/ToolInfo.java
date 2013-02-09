package cody.grblgui;

public class ToolInfo {
	float size;
	public ToolInfo(float _size) {
		size = _size;
	}
	
	public float getRadius() {
		return size * 0.5f;
	}

}
