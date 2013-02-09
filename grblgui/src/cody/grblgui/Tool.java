package cody.grblgui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Tool {
	Mesh mesh;
	public Tool() {
		float[] verts = new float[]{
				0, 0, 0, new Color(1,0,0,1).toFloatBits(),
				5, 0, 0, new Color(1,0,0,1).toFloatBits(),
				-5, 0, 0, new Color(1,0,0,1).toFloatBits(),
				0, 5, 0, new Color(1,0,0,1).toFloatBits(),
				0, -5, 0, new Color(1,0,0,1).toFloatBits()
		};
		
		
		short[] indices = new short[]{
				0,1,2,0,3,4
		};

		mesh = new Mesh(true, 5, 6, 
				new VertexAttribute(Usage.Position, 3, "a_position"), 
				new VertexAttribute(Usage.ColorPacked, 4,"a_color"));
		
		mesh.setVertices(verts);
		mesh.setIndices(indices);
	}
	
	public Vector3 position = new Vector3();
	
	public void draw(Matrix4 matrix) {
		Matrix4 m = matrix.cpy();
		m.translate(position.x, position.y, position.z);
		Util.render(mesh, GL10.GL_LINE_STRIP, m);
	}
	

}
