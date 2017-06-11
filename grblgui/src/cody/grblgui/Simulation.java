package cody.grblgui;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Simulation {
	public int count_x, count_y;
	float map[][];
	Vector2 zero;
	final float precision;
	
	public Simulation(float size_x, float size_y, float size_z, float precision) {
		this.precision = precision;
		count_x = (int)(size_x / precision);
		count_y = (int)(size_y / precision);
		
		map = new float[count_x][count_y];
		zero = new Vector2(-size_x / 2f,-size_y / 2f);
	}
	
	public Vector2 indexToPosition(int x, int y) {
		return new Vector2(zero.x + x * precision, zero.y + y * precision);
	}
	
	public GridPoint2 positionToIndex(Vector2 v) {
		return new GridPoint2((int)((v.x - zero.x) / precision), (int)((v.y - zero.y) / precision));
	}
	
	public GridPoint2 positionToIndex(float x, float y) {
		return new GridPoint2((int)((x - zero.x) / precision), (int)((y - zero.y) / precision));
	}
	
	public int lengthToCount(float l) {
		return (int)(l/precision);
	}
	
	public float getZ(int x, int y) {
		return map[x][y];
	}
	
	public Vector2 getZminmax() {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		  for(int x = 0; x <= count_x - 1; ++x) {
			  for(int y = 0; y <= count_y - 1; ++y) {
			    float z = getZ(x,y);
			    min = Math.min(z, min);
			    max = Math.max(z, max);
			  }
		  }
		  return new Vector2(min, max);
	}
	
	void mill(int x, int y, float z) {
		if(x >= 0 && y >= 0 && x < count_x && y < count_y)
			map[x][y] = Math.min(map[x][y], z);
	}
	
	void mill(Vector3 pos, ToolInfo tool) {
		GridPoint2 center = positionToIndex(pos.x, pos.y);
	  int count = lengthToCount(tool.getRadius());
	  
	  for(int x = center.x - count; x <= center.x + count; ++x) {
		  for(int y = center.y - count; y <= center.y + count; ++y) {
			  Vector2 p = indexToPosition(x,y);
			  p = new Vector2(p.x - pos.x, p.y - pos.y);
			  
			  float dist = p.len();
			  if(dist < tool.getRadius()) {
			    //println("mill " + pos.x + " " + pos.y + " " + pos.z + " " + x + " " + y)
			    mill(x, y, pos.z);
			  }
		  }
	  }
	}
	
	void simulate(Toolpath path, ToolInfo tool) {
		Vector3 cur = path.path.get(0);
		
		for(int i=1;i<=path.path.size() - 1;++i) {
				    Vector3 next = path.path.get(i);
				    if(next.z < 0 || cur.z < 0){
				      Vector3 d = next.cpy();
				      d.sub(cur);
				      //println("line " + cur.toString() + " -> " + next.toString())
				      int segments = Math.max(1,(int)(d.len() / precision));
				      for(int j = 0; j <= segments - 1; ++j) {
				        Vector3 p = d.cpy();
				        p.scl(j);
				        p.scl(1.0f/segments);
				        p.add(cur);
				        mill(new Vector3(p.x,p.y,p.z), tool);
				      }
				    }
				    cur = next;
				  }
	}
}
