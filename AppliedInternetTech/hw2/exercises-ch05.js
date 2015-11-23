/*
 * Use the reduce method in combination with the concat method to “flatten” 
 * an array of arrays into a single array that has all the elements of the 
 * input arrays.
 */
function reduceArray(array) {
	var flattened = array.reduce(flatten, []);
	return flattened;
}
function flatten(result, current) {
	return result.concat(current);
}
console.log(reduceArray([[1, 2, 3], [4, 5], [6]]));

/*
 * Every returns true only if every element matches the criteria
 */
function every(array, action) {
	var every = true;
	console.log("Running every...");
	for (var i = 0; i < array.length; i++) {
		every &= action(array[i]);
	}
	return (every) ? true : false; // To print true, instead of 1
}
function divisible3(element) {
	return element % 3 == 0 ? true: false;
}
console.log(every([9, 48, 204, 528942], divisible3));

/*
 * Some returns true if at least one element matches criteria
 */
function some(array, action) {
	console.log("Running some...");
	for (var i = 0; i < array.length; i++) {
		if (action(array[i])) {
			return true;
		}
	}
	return false;
}
function length9(string) {
	return string.length === 9 ? true : false;
}
console.log(some(['aardvark', 'abbreviate', 'abacuses', 'abandoners', 'abalones'], length9));
