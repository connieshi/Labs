function range(start, end, increment) {
	if (arguments.length == 1) {
		if (isNaN(arguments[0])) {
			console.log("Argument should be a number.");
			return;
		} 
		return rangeHelper(0, arguments[0], 1);
	} else if (arguments.length == 2) {
		if (isNaN(arguments[0]) || isNaN(arguments[1])) {
			console.log("Arguments should be a number.");
			return;
		}
		return rangeHelper(arguments[0], arguments[1], 1);
	} else if (arguments.length == 3) {
		if (isNaN(arguments[0]) || isNaN(arguments[1]) || isNaN(arguments[2])) {
			console.log("Arguments should be a number.");
			return;
		}
		return rangeHelper(arguments[0], arguments[1], arguments[2]);
	} else {
		console.log("Please provide 1, 2, or 3 arguments.");
	}
}

function rangeHelper(start, end, increment) {
	var array = [];
	var index = 0;

	if (start <= end) {
		if (increment < 0) {
			return array;
		}
		for (var i = start; i < end; i+=increment) {
			array[index++] = i;
		}
	} else {
		if (increment > 0) {
			return array;
		}
		for (var i = start; i > end; i+=increment) {
			array[index++] = i;
		}
	}
	return array;
}

console.log(range(5));
console.log(range(2, 5));
console.log(range(2, 9, 2));
console.log(range(5, 0, -1));
console.log(range(6, -1, -2));
console.log(range(6, -1, 1));
