var express = require('express');
var router = express.Router();
var mongoose = require('mongoose');
var List = mongoose.model('List');
var Item = mongoose.model('Item');

// Redirect the root page to the list page
router.get('/', function(req, res) {
  res.redirect(303, '/list');
});

// Display all lists
router.get('/list', function(req, res) {
	List.find({}, function(err, lists, count) {
		res.render('index', {lists : lists});
	});
});

// Display create-list form
router.get('/list/create', function(req, res) {
  res.render('create-list');
});

// Route handler that posts to create a new grocery list given 
// Name and createdBy
router.post('/list/create', function(req, res) {
	var list = new List({
		name: req.body.name,
		createdBy: req.body.createdBy, 
		items: []
	});

	list.save(function(err, list, count) {
		res.redirect(303, '/list/' + list.name);
	});
});

// Route handler to retrieve the page of each individual list and display items
router.get('/list/:slug', function(req, res) {
	List.findOne({slug: req.params.slug}, function(err, list, count) {
		res.render('create-item', {items: list.items, slug: req.params.slug});
	});
});

// Route handler to post to item/create form so user can enter item and quantity
router.post('/item/create', function(req, res) {
	List.findOneAndUpdate({slug: req.body.slug}, 
		{$push: {items: {
				name: req.body.name, 
				quantity: req.body.quantity, 
				checked: false
	}}}, function(err, list, count) {
		res.redirect(303, '/list/' + req.body.slug);
	});
});

// Route handler that posts to item/check form
router.post('/item/check', function(req, res) {
	List.findOne({slug: req.body.slug}, function(err, list, count) {
		console.log(req.body.checkboxes);
		var checkboxes = req.body.checkboxes;

		// If only one box is checked, the result is a string
		if (typeof checkboxes === 'string') {
			for (var i = 0; i < list.items.length; i++) {
				if (checkboxes === list.items[i].name) {
					list.items[i].checked = true;
				}
			} // If multiple boxes are checked the result is an array object
		} else if (typeof checkboxes === 'object') {
			for (var i = 0; i < list.items.length; i++) {
				for (var j = 0; j < checkboxes.length; j++) {
					if (list.items[i].name === checkboxes[j]) {
						list.items[i].checked = true;
					}
				}
			}
		}

		// Save to mongoDB
		list.markModified('items');
		list.save(function(err, modifiedItem, count) {
			res.redirect(303, '/list/' + req.body.slug);
		});
	});
});

module.exports = router;
