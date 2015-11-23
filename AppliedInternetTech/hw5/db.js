var mongoose = require('mongoose');

var Movie = new mongoose.Schema({
	title: String,
	year: Number,
	director: String
});

mongoose.model('Movie', Movie);
mongoose.connect('mongodb://localhost/hw05');
