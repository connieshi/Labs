var classYear = ['Freshman', 'Sophomore', 'Junior', 'Senior', 'Master', 'Ph.D.', 'Professional'];
var grades = ['A+', 'A', 'A-', 'B+', 'B', 'B-', 'C+', 'C', 'C-', 'D+', 'D', 'D-', 'F'];
var schools = ['New York University', 'Columbia University', 'Barnard University', 'Baruch University', 'Hunter College'];

function populateTutorFields() {
	if (document.getElementById('isTutor').checked) {
		var hidden = document.getElementsByClassName('hiddenElements');
		for (var i = hidden.length - 1; i >= 0; i--) {
			hidden[i].classList.add('shownElements');	
			hidden[i].classList.remove('hiddenElements');
		}
	} else {
		var visible = document.getElementsByClassName('shownElements');
		for (var j = visible.length - 1; j >= 0; j--) {
			visible[j].classList.add('hiddenElements');
			visible[j].classList.remove('shownElements');
		}
	}
}

function removeLastClass() {
	var classes = document.getElementById('classes');
	if (classes.childNodes.length > 1) {
		classes.removeChild(classes.lastChild);
	}
}

function addAnotherClass() {
	var group = document.createElement('div');
	group.classList.add('pure-group');
	var fieldset = document.createElement('fieldset');
	group.appendChild(fieldset);

	var className = document.createElement('input');
	className.type = 'text';
	className.name = 'aClass';
	className.placeholder = 'Class Name';

	var professor = document.createElement('input');
	professor.type = 'text';
	professor.name = 'professor';
	professor.placeholder = 'Professor';

	var semester = document.createElement('input');
	semester.type = 'text';
	semester.name = 'semester';
	semester.placeholder = 'Semester';

	var grade = document.createElement('select');
	grade.name = 'grade';
	for (var i = 0; i < grades.length; i++) {
		var val = document.createElement('option');
		var text = document.createTextNode(grades[i]);
		val.value = grades[i];
		val.appendChild(text);
		grade.appendChild(val);
	}

	fieldset.appendChild(className);
	fieldset.appendChild(professor);
	fieldset.appendChild(semester);
	fieldset.appendChild(grade);
	
	var classes = document.getElementById('classes');
	classes.appendChild(group);
}

function validateFields() {
	clearErrorNodes();
	var hasError = false;

	var username = document.getElementById('username').value;
	for (var h = 0; h < username.length; h++) {
		if (username.charAt(h) == username.charAt(h).toUpperCase()) {
			hasError = true;
			makeErrorNode('usernameError', 'Username must be all lowercase.');
		}
	}

	// Check that zipcode is 5 digits, within the US
	var zip = document.getElementById('zipcode');
	if (zip.value.length != 5) {
		makeErrorNode('zipError', 'Enter a 5 digit zipcode.');
		hasError = true;
	}

	// Check that school is valid
	var school = document.getElementById('school');
	var found = false;
	for (var i = 0; i < schools.length; i++) {
		if (school.value == schools[i]) {
			found = true;
		}
	}
	if (!found) {
		makeErrorNode('schoolError', 'Enter a valid school.');
		hasError = true;
	}

	// Check classes are filled out
	var isTutor = document.getElementById('isTutor');
	if (isTutor.checked) {
		var classes = document.getElementsByName('aClass');
		var professors = document.getElementsByName('professor');
		var semesters = document.getElementsByName('semester');
		var grades = document.getElementsByName('grade');
		for (var j = 0; j < classes.length; j++) {
			if (classes[j].value.length === 0 || professors[j].value.length === 0 || 
					semesters[j].value.length === 0 || grades[j].value.length === 0) {
				makeErrorNode('classError', 'One or more fields not filled out in Classes.');
				hasError = true;
			}
		}
	}

	// Check availability to have both start and end filled or both empty
	if (isTutor.checked) {
		var times = document.getElementsByClassName('time');
		for (var k = 0; k < times.length; k += 2) {
			if (times[k].value === '' && times[k + 1].value === '') { 
				// Both null is okay
			} else if (times[k].value === '' || times[k + 1].value === '') {
				// One null is not okay to express time intervals
				makeErrorNode('timeError', 'Time interval missing values');
				hasError = true;
			} else if (parseInt(times[k].value) >= parseInt(times[k + 1].value)) {
				makeErrorNode('intervalError', 'Starting time cannot be later than or equal to ending time');
				hasError = true;
			}
		}
	}

	if (hasError) {
		alert('One or more fields is incorrectly filled out.');
	}
	return !hasError;
}

function clearErrorNodes() {
	var errors = document.getElementById('errors');
	while (errors.firstChild) {
		errors.removeChild(errors.firstChild);
	}
}

function makeErrorNode(nodeId, text) {
	if (!document.getElementById(nodeId)) {
		var li = document.createElement('li');
		li.id = nodeId;
		li.appendChild(document.createTextNode(text));
		li.classList.add('errorMessages');
		document.getElementById('errors').appendChild(li);
	}
}
