var express = require('express');
var handlebars = require('express-handlebars').create({defaultLayout: 'main'});
var app = express();
var port = 3000;

app.engine('handlebars', handlebars.engine);
app.set('view engine', 'handlebars');

app.get('/', function(request, response) {
	response.render('index', {object: request.headers});
});

app.get('/about', function(request, response) {
	response.render('about');
});

app.get('*', function(request, response) {
	response.status(404);
	response.render('404');
});

app.listen(port);
console.log('Started server on port', port);
