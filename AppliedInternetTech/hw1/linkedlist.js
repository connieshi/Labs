function arrayToList(array) {
	var list = null;
	for (var i = array.length - 1; i >= 0; i--) {
		list = {value: array[i], rest: list};
	}
	return list;
}

function listToArray(list) {
	var array = [];
	var i = 0;

	while (list != null) {
		array[i++] = list.value;
		list = list.rest;
	}
	return array;
}

function prepend(element, list) {
	return {value: element, rest: list};
}

function nth(list, number) {
	var answer = null;
	while (number-- != 0) {
		list = list.rest;
	}
	return list.value;
}

function recursiveNth(list, number) {
	if (number == 0) {
		return list.value;
	} else {
		return recursiveNth(list.rest, number-1);
	}
}

console.log(arrayToList([10,20]));
console.log(listToArray(arrayToList([10,20,30])));
console.log(prepend(10, prepend(20,null)));
console.log(nth(arrayToList([10,20,30]), 1));
console.log(recursiveNth(arrayToList([10,20,30]), 1));
