package cody.grblgui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;

public class Part {
	public Part() {
	}
	
	/*
	Mesh generateMesh(Simulation sim) {
		float[] verts = new float[(sim.count_x() + 1) * 2 * (sim.count_y() - 1) * (3 + 1)];
		System.out.println(verts.length / (3 + 1));
		
		Tuple2<Object, Object> tmp = sim.getZminmax();
		final float min = (float)tmp._1;
		final float max = (float)tmp._2;
		
		int index = 0;
		
		for(int y = 0; y < sim.count_y() - 1; ++y) {
			for(int x = 0; x < sim.count_x(); ++x) {
				
				float pz = sim.getZ(x, y);
				Vector2 v = sim.index_to_position(x, y);
				float px = v.x;
				float py = v.y;
				float c1 = (max - pz) / (max - min);
				if(pz != max)
					c1 = Math.max(0.2f, c1);
				
				float pz2 = sim.getZ(x, y + 1);
				Vector2 v2 = sim.index_to_position(x, y + 1);
				float px2 = v2.x;
				float py2 = v2.y;
				float c2 = (max - pz2) / (max - min);
				if(pz2 != max)
					c2 = Math.max(0.2f, c2);

				
				//System.out.println(index);
				verts[index++] = px;
				verts[index++] = py;
				verts[index++] = pz;
				verts[index++] = new Color(0,0,c1,1).toFloatBits();
				verts[index++] = px2;
				verts[index++] = py2;
				verts[index++] = pz2;
				verts[index++] = new Color(0,0,c2,1).toFloatBits();
			}
			for(int i=0;i<8;++i)
				verts[index++] = verts[index-8];
		}

		System.out.print(verts.length);
		System.out.print(" = ");
		System.out.println(index);
		
		if(verts.length != index)
			throw new RuntimeException();

		Mesh mesh = new Mesh(true, verts.length / 4, 0,
				new VertexAttribute(Usage.Position, 3, "a_position"), 
				new VertexAttribute(Usage.ColorPacked, 4,"a_color"));
		
		mesh.setVertices(verts);
		
		return mesh;
	}
	
	public Part(Simulation sim) {
		mesh = generateMesh(sim);
		texture = new Texture(Gdx.files.internal("data/wood.jpg"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
	*/
	protected Mesh mesh;
	protected Texture texture;
	
	public void draw(Matrix4 matrix) {
		//m.translate(position.x, position.y, position.z);
		if(texture != null)
			texture.bind(0);
		Gdx.gl20.glDisable(GL20.GL_BLEND);
		ShaderProgram shader = getStandardShader();
		shader.begin();
		shader.setUniformi("u_texture", 0);
		shader.setUniformf("u_alpha", 1.0f);
		shader.setUniformMatrix("u_projModelView", matrix);
		//mesh.render(shader, GL20.GL_TRIANGLES);
		mesh.render(shader, GL20.GL_TRIANGLE_STRIP);
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
			" v_color = a_color; \n" +
			//" v_color = vec4(1.0,1.0,1.0,1.0); \n" +
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
			" gl_FragColor = (texture2D(u_texture, v_tex) + v_color) * u_alpha;\n" +
			"} \n";
}
