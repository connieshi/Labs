var request = require('request');

/**********************************************************************/
generateReport(null, basketballString, "Heat", "Spurs");

request('http://foureyes.github.io/csci-ua.0480-fall2015-001/homework/02/2014-06-15-heat-spurs.json', 
	function (error, response, body) {
		generateReport(JSON.parse(body), null, "Heat", "Spurs");
	}
);

request('http://foureyes.github.io/csci-ua.0480-fall2015-001/homework/02/2014-04-09-thunder-clippers.json',
	function (error, response, body) {
		generateReport(JSON.parse(body), null, "Thunder", "Clippers");
	}
);
/**********************************************************************/

function generateReport(basketballObject, basketballString, team1String, team2String) {
	if (!basketballObject || basketballString) {
		basketballObject = JSON.parse(basketballString);
		console.log("Generating report on String input...");
	} else {
		console.log("Generating report on URL input...");
	}
	
	// Filter players based on team name
	var team1 = basketballObject.filter(onTeam(team1String));
	var team2 = basketballObject.filter(onTeam(team2String));

	// Calculate final score for each team	
	var team1Points = finalScore(team1, getPoints);
	var team2Points = finalScore(team2, getPoints);
	console.log("Final Score: " + team1String + " " + team1Points + ", " + team2String + " " + team2Points);
	console.log("=====");

	// Get the player with the highest percentage of three pointers
	var totalPointsByPlayer = basketballObject.map(getPointsMap);
	var mostThreePointers = getBestThreePointerAverage(totalPointsByPlayer, basketballObject);
	console.log("* Player with highest percentage of points from three pointers: " + mostThreePointers);

	// Get total rebounds for each team to compare
	var team1Rebound = getTotalReboundsForTeam(team1);
	var team2Rebound = getTotalReboundsForTeam(team2);
	var mostRebounds = (team1Rebound > team2Rebound) ? team1String + " with " + team1Rebound : team2String + " with " + team2Rebound;
	console.log("* Team with most rebounds: " + mostRebounds);

	// Find the non guard player with the most assists
	var nonGuards = basketballObject.filter(nonGuard);
	nonGuardWithMostAssists(nonGuards);

	// Find the players with more turnovers than assists
	var badPlayers = basketballObject.filter(moreTurnoverThanAssists);
	console.log("* Players with more turnovers than assists: ");
	printPlayers(badPlayers);

	console.log("");
}

/*
 * Determines what team the player is on
 */
function onTeam(team) {
	return function(player) {
		return player.team === team;
	}
}

/*
 * Gets the final score using reduce
 */
function finalScore(array, action) {
	return array.reduce(action, 0);
}

/*
 * Function passed in reduce should have at least 2 params: accumulator, element
 * Adds up all the points to get the final score.
 */
function getPoints(accum, player) {
	return accum + player.threesMade * 3 
		+ (player.fieldGoalsMade - player.threesMade) * 2 
		+ player.freeThrowsMade;
}

/* Get the total points that each player won */
function getPointsMap(player) {
	return player.threesMade * 3
		+ (player.fieldGoalsMade - player.threesMade) * 2
		+ player.freeThrowsMade;
}

/*
 * Get the player with the best three pointer average from players who scored at least 10 points
 */
function getBestThreePointerAverage(totalPointsByPlayer, basketballObject) {
	var mostThreePointers = "";
	var highestAverage = 0;

	for (var i = 0; i < basketballObject.length; i++) {
		if (totalPointsByPlayer[i] < 10) {
			totalPointsByPlayer[i] = 0;
		} else {
			totalPointsByPlayer[i] = basketballObject[i].threesMade / totalPointsByPlayer[i];
			if (totalPointsByPlayer[i] > highestAverage) {
				mostThreePointers = basketballObject[i].name;
				highestAverage = totalPointsByPlayer[i];
			}
		}
	}
	return mostThreePointers;
}

/*
 * Add rebounds with total (accumulator)
 */
function addRebounds(total, player) {
	return total + player.offensiveRebounds + player.defensiveRebounds;
}

/*
 * Add up all rebounds in the same team
 */
function getTotalReboundsForTeam(array) {
	return array.reduce(addRebounds, 0);
}

/* Returns true if player is not a guard */
function nonGuard(player) {
	return player.position !== 'G';
}

/* Loops through to find the non guard player with the most assists */
function nonGuardWithMostAssists(array) {
	var mostAssists = 0;
	var name = "";
	for (var i = 0; i < array.length; i++) {
		if (mostAssists < array[i].assists) {
			mostAssists = array[i].assists;
			name = array[i].name;
		}
	}
	console.log("* Non guard player with most assists: " + name + " with " + mostAssists);
}

/* Returns true if player has more turnovers than assists. */
function moreTurnoverThanAssists(player) {
	return player.turnovers > player.assists;
}

/* Print out the name of the players */
function printPlayers(array) {
	array.forEach(function(player) {
		console.log(player.name);
  });
}
