package cody.grblgui

object Vect{ //Companion object for the Vector-class
	def apply(x:Float*):Vect = new Vect(x)
}
 
class Vect(val a:Seq[Float]){//Vector id is taken by std-library
 
        private case class DimensionMismatchException(a:Int,b:Int) 
           extends Throwable("Dimensions do not match: "+a+"!="+b)
 
	private def piecewise(f:(Float,Float)=>Float)(v:Vect) = 
        if (a.length!=v.a.length)
            throw DimensionMismatchException(a.length,v.a.length)
        else
            new Vect((a,v.a).zipped.map(f))
 
	def +(v:Vect) = piecewise(_+_)(v)
	def -(v:Vect) = piecewise(_-_)(v)
	def *(v:Vect) = piecewise(_*_)(v)
	def *(s:Float) = new Vect(a.map(_*s))
	def /(s:Float) = new Vect(a.map(_/s))
	def ^(s:Float) = new Vect( a.map(math.pow(_,s).toFloat) )
	def map(f:Float=>Float) = new Vect(a.map(f))
	override def toString() = "("+a.mkString(",")+")"
}
 
case class Vector3D(x:Float,y:Float,z:Float) extends Vect(Nil){
	override val a = Seq(x,y,z)
	lazy val length = math.sqrt(x*x+y*y+z*z).toFloat//Only compute if used
	def unit = Vector3D(x/length,y/length,z/length)
}

case class Vector2D(x:Float,y:Float) extends Vect(Nil){
	override val a = Seq(x,y)
	lazy val length = math.sqrt(x*x+y*y).toFloat//Only compute if used
	def unit = Vector2D(x/length,y/length)
}