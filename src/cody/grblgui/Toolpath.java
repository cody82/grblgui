package cody.grblgui;

import java.util.ArrayList;

import cody.gcode.GCodeCommand;
import cody.gcode.GCodeFile;
import cody.gcode.GCodeLine;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Toolpath {
	public ArrayList<Vector3> path = new ArrayList<Vector3>();
	public ArrayList<Integer> line = new ArrayList<Integer>();
	public ArrayList<Float> time = new ArrayList<Float>();
	public Mesh mesh;
	public int currentLine;
	public float duration;
	public static float seek_default_speed = 1500;
	
	public static Toolpath fromGCode(GCodeFile f) {
		Toolpath tp = new Toolpath();
		Vector3 current = new Vector3();
		float current_feed = 300;
		boolean seek = true;
		
		for(GCodeLine l : f.gcode) {
			float new_feed = current_feed;
			boolean new_seek = seek;
			if(l.hasG()) {
				new_seek = l.getG().arg < 0.5f;
				if(l.isGBeforePosition()) {
					seek = new_seek;
				}
			}
			if(l.hasFeed()) {
				new_feed = l.getF().arg;
				if(l.isFeedBeforePosition()) {
					current_feed = new_feed;
				}
			}
			
			if(l.hasPosition()) {
				Vector3 old_position = current.cpy();
				GCodeCommand cmd = l.getX();
				if(cmd != null)
					current.x = cmd.arg;
				cmd = l.getY();
				if(cmd != null)
					current.y = cmd.arg;
				cmd = l.getZ();
				if(cmd != null)
					current.z = cmd.arg;
				float dist = current.dst(old_position);
				float speed = seek ? seek_default_speed : current_feed;
				float time = dist / speed;
				tp.duration += time;
				tp.time.add(tp.duration);
				tp.line.add(l.line);
				tp.path.add(current.cpy());
			}
			current_feed = new_feed;
			seek = new_seek;
		}
		
		for(int i=0;i<tp.path.size();++i) {
			tp.time.set(i, tp.time.get(i) - tp.duration);
		}
		return tp;
	}
	
	public float getEta() {
		if(currentLine<time.size())
			return time.get(currentLine);
		return 0;
	}
	public void create() {
		
		mesh = new Mesh(true, path.size(), path.size(), 
				new VertexAttribute(Usage.Position, 3, "a_position"), 
				new VertexAttribute(Usage.ColorPacked, 4,"a_color"),
				new VertexAttribute(Usage.Generic, 1,"a_line")
		);

		float[] verts = new float[path.size() * 5];
		short[] indices = new short[path.size()];
		for(int i=0;i<path.size();++i) {
			verts[i * 5 + 0] = path.get(i).x; 
			verts[i * 5 + 1] = path.get(i).y; 
			verts[i * 5 + 2] = path.get(i).z; 
			verts[i * 5 + 3] = new Color(0.3f,0.3f,0,1).toFloatBits();
			verts[i * 5 + 4] = i;
			indices[i] = line.get(i).shortValue();
		}
		
		mesh.setVertices(verts);
		mesh.setIndices(indices);
	}
	
	public void draw(Matrix4 matrix) {
		if(mesh == null)
			create();
		Matrix4 m = matrix.cpy();
		//m.translate(position.x, position.y, position.z);
		//Util.render(mesh, GL20.GL_LINE_STRIP, m);
		
		ShaderProgram shader = getStandardShader();
		shader.begin();
		shader.setUniformf("u_alpha", 1);
		shader.setUniformf("u_line", currentLine);
		shader.setUniformMatrix("u_projModelView", m);
		mesh.render(shader, GL20.GL_LINE_STRIP);
		shader.end();
	}

	static ShaderProgram standardShader;
	
	public static ShaderProgram getStandardShader() {
		if(standardShader != null){
			return standardShader;
		}
		standardShader = new ShaderProgram(standardVertexShader, standardFragmentShader);
		
		return standardShader;
	}
	
	static final String standardVertexShader = "attribute vec4 a_position; \n" +
			"attribute vec4 a_color; \n" +
			"uniform mat4 u_projModelView; \n" + 
			"uniform float u_line; \n" + 
			"attribute float a_line; \n" + 
			"varying vec4 v_color; \n" + 
			"void main() \n" +
			"{ \n" +
			" gl_Position = u_projModelView * a_position; \n" +
			//" v_color = a_color; \n" +
			" v_color = a_line < u_line ? vec4(0.0,1.0,0.0,1.0) : a_color; \n" +
			"} \n";

	static final String standardFragmentShader = "#ifdef GL_ES\n" +
			"precision mediump float;\n" +
			"#endif\n" +
			"uniform float u_alpha; \n" + 
			"varying vec4 v_color; \n" + 
			"void main() \n" +
			"{ \n" +
			" gl_FragColor = v_color * u_alpha;\n" +
			"} \n";
}
