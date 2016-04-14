var express = require('express');
var mongoose = require('mongoose');
var User = mongoose.model('User');
var Class = mongoose.model('Class');
var Message = mongoose.model('Message');
var Schedule = mongoose.model('Schedule');
var TimeInterval = mongoose.model('TimeInterval');
var router = express.Router();

/* Home page to sign up or log in */
router.get('/', function(req, res) {
	req.session.username = '';
  res.render('index', {title: 'Tutes'});
});

/* Log in */
router.post('/', function(req, res) {
	User.findOne({'username': req.body.username, 'password': req.body.password}, 
		function(err, user) {
		if (user) {
			req.session.username = req.body.username;
			res.redirect(303, 'tutors');
		} else {
			res.render('index', {title: 'Tutes', logInError: 
				"Your username or password is not found."});
		}
	});
});

/* Sign up as a tutor or tutee */
router.get('/signup', function(req, res) {
	res.render('signup');
});

/* Submit the sign up form */
router.post('/signup', function(req, res) {
	User.findOne({'username': req.body.username}, function(err, person) {
		if (!person) {
			var user = new User({
				username: req.body.username,
				password: req.body.password,
				name: req.body.name,
				zipcode: req.body.zipcode,
				school: req.body.school,
				classYear: req.body.classYear,
				aboutMe: req.body.aboutme,
			});

			createClasses(user, req);
			createSchedule(user, req);
			user.save(function(err, person) {
				req.session.username = req.body.username;
				res.redirect(303, 'tutors');
			}); 
		} else {
			res.render('signup', {title: 'Tutes', signupError: 
				'Username is already taken.'});
		}
	});
});

function createClasses(user, req) {
	var tutor = req.body.tutor;
	if (typeof tutor === 'string' && tutor.length > 0) {
		user.tutor = true;
		user.gpa = req.body.gpa;
		user.compensation = req.body.compensation;
		if (typeof req.body.aClass === 'string') {
			var c1 = new Class({
				className: req.body.aClass,
				professor: req.body.professor,
				semesterTaken: req.body.semester,
				grade: req.body.grade
			});
			c1.save(function(err, data) {
				user.classes.push(c1);
			});
		} else {
			var allClasses = [];
			for (var i = 0; i < req.body.aClass.length; i++) {
				var c2 = new Class({
					className: req.body.aClass[i],
					professor: req.body.professor[i],
					semesterTaken: req.body.semester[i],
					grade: req.body.grade[i]
				});
				allClasses.push(c2);
			}
			for (var j = allClasses.length - 1; j >= 0; j--) {
				allClasses[j].save();
				user.classes.push(allClasses[j]);
			}
		}
	}
}

function createSchedule(user, req) {
	var tutor = req.body.tutor;
	if (typeof tutor === 'string' && tutor.length > 0) {
		var schedule = new Schedule({});
		
		// Monday
		if (req.body.mondayStart.length > 0) {
			var mon = new TimeInterval({
				start: req.body.mondayStart,
				end: req.body.mondayEnd
			});
			mon.save();
			schedule.monday.push(mon);
		} 
		// Tuesday
		if (req.body.tuesdayStart.length > 0) {
			var tues = new TimeInterval({
				start: req.body.tuesdayStart,
				end: req.body.tuesdayEnd
			});
			tues.save();
			schedule.tuesday.push(tues);
		}
		// Wednesday
		if (req.body.wednesdayStart.length > 0) {
			var weds = new TimeInterval({
				start: req.body.wednesdayStart,
				end: req.body.wednesdayEnd
			});
			weds.save();
			schedule.wednesday.push(weds);
		}
		// Thursday
		if (req.body.thursdayStart.length > 0) {
			var thurs = new TimeInterval({
				start: req.body.thursdayStart,
				end: req.body.thursdayEnd
			});
			thurs.save();
			schedule.thursday.push(thurs);
		}	
		// Friday
		if (req.body.fridayStart.length > 0) {
			var fri = new TimeInterval({
				start: req.body.fridayStart,
				end: req.body.fridayEnd
			});
			fri.save();
			schedule.friday.push(fri);
		}
		// Saturday
		if (req.body.saturdayStart.length > 0) {
			var sat = new TimeInterval({
				start: req.body.saturdayStart,
				end: req.body.saturdayEnd
			});
			sat.save();
			schedule.saturday.push(sat);
		}
		// Sunday
		if (req.body.sundayStart.length > 0) {
			var sun = new TimeInterval({
				start: req.body.sundayStart,
				end: req.body.sundayEnd
			});
			sun.save();
			schedule.sunday.push(sun);
		}
		schedule.save(function(err, data) {
			user.availability = schedule;
		});
	}
}

/* Show all tutors 
router.get('/tutors', function(req, res) {
	User.find({'tutor' : 'true'}, function(err, tutors) {
		res.json(tutors.map(function(ele) {
      return {
        'slug': ele.slug,
				'name': ele.name,
				'username': ele.username,
				'school': ele.school,
      };
    }));
	});
}); */

/* Show all tutors */
router.get('/tutors', function(req, res) {
	User.find({'tutor' : 'true'}, function(err, tutors) {
		res.render('tutors', {tutors: tutors, username: req.session.username});
	});
});

router.post('/tutors', function(req, res) {
	var tutorUsername = req.body.tutorUsername;
	User.find({'tutor': 'true', 'username': tutorUsername}, function(err, tutors) {
		res.render('tutors', {tutors: tutors, username: req.session.username});
	});
});

/* Render the profile of the tutor */
router.get('/tutors/:slug', function(req, res) {
	User.findOne({slug: req.params.slug}, function(err, tutor) {

		var classIds = [];		
		for (var i = 0; i < tutor.classes.length; i++) {
			classIds.push(tutor.classes[i]);
		}

		var classes = [];
		var pushClasses = function(err, aClass) {
			classes.push(aClass);
		};
		for (var j = 0; j < classIds.length; j++) {
			Class.find({'_id' : classIds[j]}, pushClasses);
		}

		var availability = {};
		Schedule.findOne({'_id' : tutor.availability}, function(err, schedule) {
			if (schedule.monday.length !== 0) {
				TimeInterval.find({'_id': schedule.monday}, function(err, day) {
					availability.monday = day;
				});
			}
			if (schedule.tuesday.length !== 0) {
				TimeInterval.find({'_id': schedule.tuesday}, function(err, day) {
					availability.tuesday = day;
				});
			}
			if (schedule.wednesday.length !== 0) {
				TimeInterval.find({'_id': schedule.wednesday}, function(err, day) {
					availability.wednesday = day;
				});
			}
			if (schedule.thursday.length !== 0) {
				TimeInterval.find({'_id': schedule.thursday}, function(err, day) {
					availability.thursday = day;
				});
			}
			if (schedule.friday.length !== 0) {
				TimeInterval.find({'_id': schedule.friday}, function(err, day) {
					availability.friday = day;
				});
			}
			if (schedule.saturday.length !== 0) {
				TimeInterval.find({'_id': schedule.saturday}, function(err, day) {
					availability.saturday = day;
				});
			}
			if (schedule.sunday.length !== 0) {
				TimeInterval.find({'_id': schedule.sunday}, function(err, day) {
					availability.sunday = day;
				});
			}
			res.render('profile', {person: tutor, classes: classes, availability: availability});
		});
	});
});

router.get('/message', function(req, res) {
	if (req.session.username) {
		var messageUsers = [];
		Message.find({$or: [{'from': req.session.username}, 
				{'to': req.session.username}]}, function(err, messages) {
			if (messages) {
				for (var i = 0; i < messages.length; i++) {
					if (messages[i].from == req.session.username) {
						if (messageUsers.indexOf(messages[i].to) == -1) {
							messageUsers.push(messages[i].to);
						}
					} else if (messages[i].to == req.session.username) {
						if (messageUsers.indexOf(messages[i].from) == -1) {
							messageUsers.push(messages[i].from);
						}
					}
				}
				res.render('message', {messageUsers: messageUsers});
			}
		});
	} else {
		res.redirect(303, '/');
	}
});

router.get('/message/:otherUser', function(req, res) {
	if (req.session.username) {
		Message.find({$or: [{'from': req.session.username, 'to': req.params.otherUser},
				{'from': req.params.otherUser, 'to': req.session.username}]})
			.sort({'timestamp': -1}).lean().exec(function(err, messages) {
				var jsonObject = messages;
				for (var i = 0; i < jsonObject.length; i++) {
					if (jsonObject[i].from == req.session.username) {
						jsonObject[i].sent = true;
					}
				}
				res.render('chat', {message: jsonObject, currentUser: req.session.username});
		});
	} else {
		res.redirect(303, '/');
	}
});

router.post('/message/:otherUser', function(req, res) {
	if (req.session.username) {
		console.log(req.session.username, req.params.otherUser);
		var m = new Message({
			from: req.session.username,
			to: req.params.otherUser,
			message: req.body.message
		});
		m.save(function(err, data) {
			res.redirect(303, '/message/' + req.params.otherUser);
		});
	} else {
		res.redirect(303, '/');
	}
});

module.exports = router;
