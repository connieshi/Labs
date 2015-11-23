require('./db');
var mongoose = require('mongoose');
var express = require('express');
var handlebars = require('express-handlebars').create({defaultLayout:'main'});

var app = express();
var Movie = mongoose.model('Movie');
app.engine('handlebars', handlebars.engine);
app.set('view engine', 'handlebars');

app.get('/movies', function (req, res) {
	var searchCriteria = {};

	if (req.query.director != null) {
		searchCriteria.director = req.query.director;
	}

	Movie.find(searchCriteria, function(err, data, count) {
		res.render('movies', {movies : data});
	});
});

app.listen(3000);
