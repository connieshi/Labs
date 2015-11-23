var mongoose = require('mongoose');
var URLSlugs = require('mongoose-url-slugs');

// Schema for Item object
var Item = new mongoose.Schema({
	name: String,
	quantity: Number,
	checked: Boolean
});

// Schema for List object that contains items
var List = new mongoose.Schema({
	name: String,
	createdBy: String,
	items: [Item]
});

List.plugin(URLSlugs('name'));
mongoose.model('List', List);
mongoose.model('Item', Item);
mongoose.connect('mongodb://localhost/grocerydb');
