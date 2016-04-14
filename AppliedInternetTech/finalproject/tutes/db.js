var mongoose = require('mongoose');
var URLSlugs = require('mongoose-url-slugs');

/*
 * Used to represent a user profile, both tutors and tutees.
 * Tutees who do not wish to tutor can set Boolean tutor to false, and subsequently
 * Will not be asked to fill out parts of the form specific to tutors.
 */
var User = new mongoose.Schema({
		username: String,
		password: String,
    tutor: Boolean,
		name: String,
    zipcode: Number,
    school: String,
    classYear: String,
    aboutMe: String,
    gpa: Number,
    compensation: Number,
    classes: [{type: mongoose.Schema.Types.ObjectId, ref: 'Class'}],
    availability: {type: mongoose.Schema.Types.ObjectId, ref: 'Schedule'}
});

/*
 * Represents a class that the tutor is comfortable tutoring, including the professor,
 * semester taken, and the grade received from the class.
 */
var Class = new mongoose.Schema({
    className: String,
    professor: String,
    semesterTaken: String,
    grade: String
});

/*
 * A message sent from one person to another. Will be stored within the User object
 * of the recipient of the message.
 */
var Message = new mongoose.Schema({
    from: String, 
		to: String,
    timestamp: {type: Date, default: Date.now},
    message: String
});

/*
 * Schedule for availability to tutor for all days of the week.
 */
var Schedule = new mongoose.Schema({
    monday: [{type: mongoose.Schema.Types.ObjectId, ref: 'TimeInterval'}],
    tuesday: [{type: mongoose.Schema.Types.ObjectId, ref: 'TimeInterval'}],
    wednesday: [{type: mongoose.Schema.Types.ObjectId, ref: 'TimeInterval'}],
    thursday: [{type: mongoose.Schema.Types.ObjectId, ref: 'TimeInterval'}],
    friday: [{type: mongoose.Schema.Types.ObjectId, ref: 'TimeInterval'}],
    saturday: [{type: mongoose.Schema.Types.ObjectId, ref: 'TimeInterval'}],
    sunday: [{type: mongoose.Schema.Types.ObjectId, ref: 'TimeInterval'}],
});

/*
 * The starting and ending time that a user is available to tutor in any day.
 */
var TimeInterval = new mongoose.Schema({
    start: Number,
    end: Number,
});

User.plugin(URLSlugs('name'));
mongoose.model('User', User);
mongoose.model('Class', Class);
mongoose.model('Message', Message);
mongoose.model('Schedule', Schedule);
mongoose.model('TimeInterval', TimeInterval);
mongoose.connect('mongodb://localhost:10541/tutesdb');
