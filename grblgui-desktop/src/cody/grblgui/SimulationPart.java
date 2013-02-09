package cody.grblgui;

import java.io.FileNotFoundException;

import scala.Tuple2;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Vector2;

import cody.grblgui.Part;

public class SimulationPart extends Part {


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
	
	public SimulationPart(Simulation sim) {
		mesh = generateMesh(sim);
		texture = new Texture(Gdx.files.internal("data/wood.jpg"));
		texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
		texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	}
}
