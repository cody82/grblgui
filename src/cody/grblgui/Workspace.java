package cody.grblgui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Plane;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

public class Workspace {
	Mesh mesh;
	public Workspace(float x1, float x2, float y1, float y2, float z1, float z2) {
		float[] verts = new float[]{
				x1, y1, z1, new Color(1,1,1,1).toFloatBits(),
				x1, y2, z1, new Color(1,1,1,1).toFloatBits(),
				x2, y2, z1, new Color(1,1,1,1).toFloatBits(),
				x2, y1, z1, new Color(1,1,1,1).toFloatBits(),
		};
		
		
		short[] indices = new short[]{
				0,1,2,3,0
		};

		mesh = new Mesh(true, 4, 5, 
				new VertexAttribute(Usage.Position, 3, "a_position"), 
				new VertexAttribute(Usage.ColorPacked, 4,"a_color"));
		
		mesh.setVertices(verts);
		mesh.setIndices(indices);
	}
	
	public void draw(Matrix4 matrix) {
		Util.render(mesh, GL10.GL_LINE_STRIP, matrix);
	}
	
	public Vector3 intersect(Ray ray) {
		Plane p = new Plane(new Vector3(0,0,1), new Vector3(0,0,0));
		Vector3 pos = new Vector3();
		Intersector.intersectRayPlane(ray, p, pos);
		return pos;
	}

}
