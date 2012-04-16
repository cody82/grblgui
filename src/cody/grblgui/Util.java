package cody.grblgui;

import java.util.ArrayList;
import java.util.List;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;


public class Util {

	public static ShaderProgram getStandardShader() {
		if(standardShader != null){
			return standardShader;
		}
		standardShader = new ShaderProgram(standardVertexShader, standardFragmentShader);
		
		return standardShader;
	}
	
	public static void render(Mesh mesh, int primitiveType, Matrix4 projModelView) {
		render(mesh, primitiveType, projModelView, 1f);
	}
	
	public static void render(Mesh mesh, int primitiveType, Matrix4 projModelView, float alpha) {
		if(Gdx.graphics.isGL20Available()) {
			ShaderProgram shader = getStandardShader();
			shader.begin();
			shader.setUniformf("u_alpha", alpha);
			shader.setUniformMatrix("u_projModelView", projModelView);
			mesh.render(shader, primitiveType);
			shader.end();
		}
		else {
			GL10 gl = Gdx.graphics.getGL10();
			gl.glMatrixMode(GL10.GL_PROJECTION);
			gl.glLoadIdentity();
			gl.glMatrixMode(GL10.GL_MODELVIEW);
			gl.glLoadMatrixf(projModelView.getValues(),0);
			mesh.render(primitiveType);
		}
	}

	static ShaderProgram standardShader;
	
	static final String standardVertexShader = "attribute vec4 a_position; \n" +
			"attribute vec4 a_color; \n" +
			"uniform mat4 u_projModelView; \n" + 
			"varying vec4 v_color; \n" + 
			"void main() \n" +
			"{ \n" +
			" gl_Position = u_projModelView * a_position; \n" +
			" v_color = a_color; \n" +
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
