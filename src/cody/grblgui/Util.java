package cody.grblgui;

import com.badlogic.gdx.graphics.Mesh;
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
		ShaderProgram shader = getStandardShader();
		shader.begin();
		shader.setUniformf("u_alpha", alpha);
		shader.setUniformMatrix("u_projModelView", projModelView);
		mesh.render(shader, primitiveType);
		shader.end();
	}

	static ShaderProgram standardShader;
	
	static final String standardVertexShader = "attribute vec4 a_position; \n" +
			"attribute vec4 a_color; \n" +
			"uniform mat4 u_projModelView; \n" + 
			"varying vec4 v_color; \n" + 
			"void main() \n" +
			"{ \n" +
			" gl_Position = u_projModelView * a_position; \n" +
			//" v_color = a_color; \n" +
			" v_color = vec4(1.0,1.0,1.0,1.0); \n" +
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
