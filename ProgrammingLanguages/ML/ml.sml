Control.Print.printDepth := 100;
Control.Print.printLength := 100;

(* 1 *)
fun partition x [] = ([], [])
  | partition x (y::ys) =
		let val (less, more) = partition x ys
in
	if (op <=) (x, y) then (less, y::more) else (y::less, more)
end

(* 2 *)
fun partitionSort [] = []
	| partitionSort [L] = [L]
	| partitionSort (x::xs) = 
		let val (lessThan, greaterThan) = partition x xs;
in 
	(partitionSort lessThan) @ [x] @ (partitionSort greaterThan)
end

(* 3 *)
fun Sort f [] = []
	| Sort f [L] = [L]
	| Sort f (x::xs) = 
		let
			fun partition f x [] = ([], [])
				| partition f x (y::ys) = 
					let val (less, more) = partition f x ys
			in
				if f (x, y) then (less, y::more) else (y::less, more)
			end
			val (one, two) = partition f x xs
in 
	(Sort f one) @ [x] @ (Sort f two)
end

(* 4 *)
datatype 'a tree = empty | leaf of 'a | node of 'a * 'a tree * 'a tree
val myTree = node(5,node(2,leaf 3,empty), node(12,node(8,leaf 7,leaf 11), node(4,leaf 1, node(6,empty, leaf 9))))

(* 5 *)
exception EmptyTreeException

fun isEmpty (empty) = true
	| isEmpty (leaf a) = false
	| isEmpty (node (x, y, z)) = false;

fun maxTree f (empty) = raise EmptyTreeException
	| maxTree f (leaf a) = a
	| maxTree f (node (x, y, z)) =
		if (isEmpty y) then 
			if f (x, maxTree f z) 
				then maxTree f z else x
		else if (isEmpty z) then
			if f (x, maxTree f y)
				then maxTree f y else x
		else if f (x, maxTree f y) then
			if f (maxTree f y, maxTree f z)
				then maxTree f z else maxTree f y
			else
				if f (x, maxTree f z) 
					then maxTree f z else x

(* 6 *)
fun inorder x L1 L2 = L1 @ [x] @ L2
fun preorder x L1 L2 = [x] @ L1 @ L2
fun postorder x L1 L2 = L1 @ L2 @ [x]

fun Labels f (empty) = []
	| Labels f (leaf a) = [a]
	| Labels f (node (x, y, z)) = f x (Labels f y) (Labels f z)

(* 7 *)
fun isEqual f (x, y) = 
	if (f (x, y)) then false
	else if (f (y, x)) then false
		else true

fun lexLess f [] [] = true
	| lexLess f [] L2 = true
	| lexLess f L1 [] = false
	| lexLess f (x::xs) (y::ys) = 
			if f (x, y) then true
			else if isEqual f (x, y) then lexLess f xs ys
			else false

(* 8 *)
fun sortTreeList f [] = []
	| sortTreeList f [L] = [L]
	| sortTreeList f (x::xs) = 
		let
			fun partition f x [] = ([], [])
				| partition f x (y::ys) = 
					let val (less, more) = partition f x ys
			in
				if lexLess f (Labels inorder x) (Labels inorder y)
					then (less, y::more) 
				else (y::less, more)
			end
			val (less, more) = partition f x xs
in 
	(sortTreeList f less) @ [x] @ (sortTreeList f more)
end

(* Labels are [1,2,3,4,5,6,7] *)
val t1 = node(5,node(4,node(2,leaf 1, leaf 3), empty), node (7,leaf 6, empty))

(* Labels are [0,1,2,3,4,5,6,7] *)
val t2 = node (5,node(4,node (2,node(1,leaf 0, empty),leaf 3),empty), node (7,leaf 6,empty))

(* Labels are [1,2,3,4,5,6] *)
val t3 = node (5,node(4,node(2,leaf 1, leaf 3), empty), leaf 6)
