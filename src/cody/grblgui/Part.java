package cody.grblgui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class Part {
	public Part(String obj_file, String texture_file) throws FileNotFoundException {
		mesh = ObjLoader.loadObj(new FileInputStream(obj_file), true, true);
		texture = new Texture(Gdx.files.internal("data/wood.jpg"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
	
	Mesh mesh;
	Texture texture;
	
	public void draw(Matrix4 matrix) {
		Matrix4 m = matrix.cpy();
		//m.translate(position.x, position.y, position.z);
		if(texture != null)
			texture.bind(0);
		Gdx.gl20.glDisable(GL20.GL_BLEND);
		ShaderProgram shader = getStandardShader();
		shader.begin();
		shader.setUniformi("u_texture", 0);
		shader.setUniformf("u_alpha", 1.0f);
		shader.setUniformMatrix("u_projModelView", matrix);
		mesh.render(shader, GL20.GL_TRIANGLES);
		shader.end();
		Gdx.gl20.glEnable(GL20.GL_BLEND);
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
			"varying vec4 v_color; \n" + 
			"varying vec2 v_tex; \n" + 
			"void main() \n" +
			"{ \n" +
			" gl_Position = u_projModelView * a_position; \n" +
			" v_color = vec4(1.0,1.0,1.0,1.0); \n" +
			" v_tex = a_position.xy / 60.0; \n" +
			"} \n";

	static final String standardFragmentShader = "#ifdef GL_ES\n" +
			"precision mediump float;\n" +
			"#endif\n" +
			"uniform float u_alpha; \n" + 
			"uniform sampler2D u_texture; \n" + 
			"varying vec2 v_tex; \n" + 
			"varying vec4 v_color; \n" + 
			"void main() \n" +
			"{ \n" +
			" gl_FragColor = texture2D(u_texture, v_tex) * v_color * u_alpha;\n" +
			"} \n";
}
