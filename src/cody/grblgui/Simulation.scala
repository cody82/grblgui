package cody.grblgui

import Array.ofDim

class Simulation(size_x : Float, size_y : Float, size_z : Float, precision : Float) {
	val count_x : Int = (size_x / precision).toInt;
	val count_y : Int = (size_y / precision).toInt;
	var map:Array[Array[Float]] = ofDim[Float](count_x,count_y)
	var zero = Vector2D(-size_x / 2f,-size_y / 2f)
	
	def index_to_position(x : Int, y : Int) : Vector2D = Vector2D(zero.x + x * precision, zero.y + y * precision)
	
	def position_to_index(v : Vector2D) = (((v.x - zero.x) / precision).toInt, ((v.y - zero.y) / precision).toInt)
	def position_to_index(x : Float, y : Float) = (((x - zero.x) / precision).toInt, ((y - zero.y) / precision).toInt)
	def length_to_count(l : Float) : Int = (l / precision).toInt
	
	def getZ(x : Int, y : Int) = map(x)(y)
	def getZminmax() : (Float, Float) = {
	  var min : Float = Float.MaxValue
	  var max : Float = Float.MinValue
	  for(x <- 0 to count_x - 1) {
		  for(y <- 0 to count_y - 1) {
		    val z = getZ(x,y)
		    min = math.min(z, min)
		    max = math.max(z, max)
		  }
	  }
	  return (min, max)
	}
	
	def mill(x : Int, y : Int, z : Float) {
	  if(x >= 0 && y >= 0 && x < count_x && y < count_y)
		  map(x)(y) = math.min(map(x)(y), z)
	}
	
	def mill(pos : Vector3D, tool : ToolInfo) {
	  val center = position_to_index(pos.x, pos.y)
	  val count = length_to_count(tool.getRadius())
	  for(x <- center._1 - count to center._1 + count) {
		  for(y <- center._2 - count to center._2 + count) {
			  var p = index_to_position(x,y)
			  p = Vector2D(p.x - pos.x, p.y - pos.y)
			  
			  val dist = p.length
			  if(dist < tool.getRadius()) {
			    //println("mill " + pos.x + " " + pos.y + " " + pos.z + " " + x + " " + y)
			    mill(x, y, pos.z)
			  }
		  }
	  }
	}
	
	def simulate(path : Toolpath, tool : ToolInfo) {
	  var cur = path.path.get(0)
	  for(i <- 1 to path.path.size() - 1) {
	    val next = path.path.get(i)
	    if(next.z < 0 || cur.z < 0){
	      var d = next.cpy()
	      d.sub(cur)
	      //println("line " + cur.toString() + " -> " + next.toString())
	      val segments = math.max(1,(d.len() / precision).toInt)
	      for(j <- 0 to segments - 1) {
	        var p = d.cpy()
	        p.mul(j)
	        p.div(segments)
	        p.add(cur)
	        mill(Vector3D(p.x,p.y,p.z), tool)
	      }
	    }
	    cur = next
	  }
	}
	
	
}