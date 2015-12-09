abstract class Tree[+T]

case class Node[T](label: T, left: Tree[T], right: Tree[T]) extends Tree[T]
case class Leaf[T](label: T) extends Tree[T]

trait Addable[T] {
	def +(other: T): T
}

class A(a: Int) extends Addable[A] {
	val value: Int = a
	override def +(other: A): A = new A(value + other.value)
	override def toString(): String = "A(" + value + ")"
}

class B(b: Int) extends A(b) {
	override val value: Int = b
	override def toString(): String = "B(" + value + ")"
}

class C(c: Int) extends B(c) {
	override val value: Int = c
	override def toString(): String = "C(" + value + ")"
}

object Part2 {
	def inOrder[T](tree: Tree[T]): List[T] = {
		tree match {
			case Node(label, left, right) => inOrder(left) ++ List(label) ++ inOrder(right)
			case Leaf(label) => List(label)
		}
	}

	def treeSum[T <: Addable[T]](tree: Tree[T]): T = {
		tree match {
			case Node(label, left, right) => treeSum(left) + label + treeSum(right)
			case Leaf(label) => label
		}
	}

	def treeMap[T,V](f: T=>V, tree: Tree[T]): Tree[V] = {
		tree match {
			case Node(label, left, right) => new Node(f(label), treeMap(f, left), treeMap(f, right))
			case Leaf(label) => new Leaf(f(label))
		}
	}

	def BTreeMap(f: B=>B, tree: Tree[B]): Tree[B] = {
		tree match {
			case Node(label, left, right) => new Node(f(label), treeMap(f, left), treeMap(f, right))
			case Leaf(label) => new Leaf(f(label))
		}
	}

	def test() {
	    def faa(a:A):A = new A(a.value+10)
	    def fab(a:A):B = new B(a.value+20)
	    def fba(b:B):A = new A(b.value+30)
	    def fbb(b:B):B = new B(b.value+40)
	    def fbc(b:B):C = new C(b.value+50)
	    def fcb(c:C):B = new B(c.value+60)
	    def fcc(c:C):C = new C(c.value+70)
	    def fac(a:A):C = new C(a.value+80)
	    def fca(c:C):A = new A(c.value+90)

	    val myBTree: Tree[B] = Node(new B(4),Node(new B(2),Leaf(new B(1)),Leaf(new B(3))), 
			               Node(new B(6), Leaf(new B(5)), Leaf(new B(7))))

	    val myATree: Tree[A] = myBTree

	    println("inOrder = " + inOrder(myATree))
	    println("Sum = " + treeSum(myATree))

		// Function is covarient for return type, A is not a subtype of B
	    //println(BTreeMap(faa,myBTree))

	    println(BTreeMap(fab,myBTree))

		// Function is covarient for return type
		//println(BTreeMap(fba,myBTree)) 
	    
	    println(BTreeMap(fbb,myBTree))
	    println(BTreeMap(fbc,myBTree))

	    // Function is contravarient for input type
		// Can't convert a C to a B
		//println(BTreeMap(fcb,myBTree))

		// Function is contravarient for input type
	    //println(BTreeMap(fcc,myBTree))

	    println(BTreeMap(fac,myBTree))

		// Function is contravarient for input type
	    //println(BTreeMap(fca,myBTree))

	    println(treeMap(faa,myATree))
	    println(treeMap(fab,myATree))

		// Function expects an A and input type is B
	    //println(treeMap(fba,myATree))

		// Function expects an A and input type is B
	    //println(treeMap(fbc,myATree))

		// Function expects an A and input type is C
	    //println(treeMap(fcb,myATree))

		// Function expects an A and input type is C
	    //println(treeMap(fcc,myATree))

	    println(treeMap(fac,myATree))

		// Function expects an A and input type is C
	    //println(treeMap(fca,myATree))
  	}

	def main(args: Array[String]) = {
		test();
	}
}
