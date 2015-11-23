var path = require('path');
var express = require('express');
var session = require('express-session');
var handlebars = require('express-handlebars').create({defaultLayout:'main'});
var bodyParser = require('body-parser');

/* Setting up modules */
var app = express();
var publicPath = path.resolve(__dirname, 'public');
var port = 3000;
app.engine('handlebars', handlebars.engine);
app.set('view engine', 'handlebars');

/* Session option to use for express-session */
var sessionOptions = {
	secret:'password',
	resave:true,
	saveUninitialized:true
};

/* Using modules */
app.use(express.static(publicPath));
app.use(bodyParser.urlencoded({extended:false}));
app.use(session(sessionOptions));

/* Constructor for Bird */
var Bird = function(name, number) {
	this.name = name;
	this.number = number;
}

/* Create global array to store the initial values */
var birds = [];
birds.push(new Bird('bald eagle', 3));
birds.push(new Bird('yellow billed duck', 7));
birds.push(new Bird('great cormorant', 4));

/* Initial landing page */
app.get('/', function(request, response) {
	response.render('index');
});

/* Submitting a min value in settings page shouly only display birds 
 * with count >= min value 
*/
app.post('/settings', function(request, response) {
	print(request);
	request.session.min = request.body.min;
	var setMin = birds.filter(function(bird) {
		return bird.number >= request.session.min;
	});
	response.render('birds', {birdList : setMin});
});

/* Viewing the settings page */
app.get('/settings', function(request, response) {
	print(request);
	response.render('settings', {minValue: request.session.min});
});

/* Adding a new bird or incrementing the count of an existing one.
 * This method is case insensitive 
 */
app.post('/birds', function(request, response) {
	var foundBird = false;
	var newBird = request.body.bird.toLowerCase();
	print(request);

	// Look for a match with bird name
	for (var i = 0; i < birds.length; i++) {
		if (newBird === birds[i].name) {
			birds[i].number++;
			foundBird = true;
			break;
		}
	}

	// If bird is not found in list, add it as a new bird
	if (!foundBird) {
		birds.push(new Bird(newBird, 1));
	}

	response.redirect(303, '/birds');
});

/* Get list of birds */
app.get('/birds', function(request, response) {
	print(request);
	response.render('birds', {birdList : birds});
});

/* Print out logs */
function print(request) {
	console.log();
	console.log(request.method, request.url);
	console.log("=====");
	console.log("request.body:", request.body);
	console.log("request.session.min:", request.session.min);
	console.log();
}

app.listen(port);
console.log('Started server on port', port);
