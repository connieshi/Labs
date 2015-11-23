/* Player object with name, previous moves, and opponent's previous moves */
function Player(name) {
	this.name = name;
	this.previousMoves = [];
	this.oppPreviousMoves = [];

	/* Make the next move */
	this.move = function move() {
		var nextMove = this.getNextMove();
		console.log("\tAll of", this.name + "'s", "moves so far:", this.previousMoves);
		console.log("\tAll of her opponent's moves so far:", this.oppPreviousMoves);
		return nextMove;
	}

	/* Player object just picks rock every time */
	this.getNextMove = function getNextMove() {
		console.log("\t" + this.name, "always plays rock, regardless of her opponent's last move:", "R");
		this.previousMoves.push('R');
		return 'R';
	}

	/* Save opponent's move in player's history */
	this.recordOpponentMove = function recordOpponentMove(opponentMove) {
		this.oppPreviousMoves.push(opponentMove);
	}
}

/* Player that makes a move depending on other player's last move */
function HistoryRepeatsItselfPlayer(name) {
	Player.call(this, name);

	/* Tries to win based on opponent's last move */
	this.getNextMove = function getNextMove() {
		var length = this.oppPreviousMoves.length;

		if (length > 0) {
			var oppLastMove = this.oppPreviousMoves[length - 1];
			switch (oppLastMove) {
				case 'R':
					return this.playSmart('R', 'P');
				case 'P': 
					return this.playSmart('P', 'S');
				case 'S': 
					return this.playSmart('S', 'R');
			}
		} else { // Game just started, pick random
			var moves = ['R', 'P', 'S'];
			var random = parseInt(Math.floor((Math.random() * 3)));
			this.previousMoves.push(moves[random]);
			console.log("\t" + this.name + "'s", "first move should be random:", moves[random]);
			return moves[random];
		}
	}

	/* Print smart move and add to history */
	this.playSmart = function playSmart(oppLastMove, smartNextMove) {
		console.log("\tIf her last opponent's move was", oppLastMove, "she'll play:", smartNextMove);
		this.previousMoves.push(smartNextMove); 
		return smartNextMove;
	}	
}

/* Determines who wins depending on current hand played */
function whoWins(player1, player2) {
	var p1Move = player1.previousMoves[player1.previousMoves.length - 1];
	var p2Move = player2.previousMoves[player2.previousMoves.length - 1];

	if (p1Move == p2Move) {
		console.log("\tIt's a tie!\n");
	}

	switch (p1Move) {
		case 'R':
			if (p2Move == 'P') console.log("\t" + player2.name, "Wins!\n");
			else if (p2Move == 'S') console.log("\t" + player1.name, "Wins!\n");
			break;
		case 'S':
			if (p2Move == 'P') console.log("\t" + player1.name, "Wins!\n");
			else if (p2Move == 'R') console.log("\t" + player2.name, "Wins!\n");
			break;
		case 'P':
			if (p2Move == 'S') console.log("\t" + player2.name, "Wins!\n");
			else if (p2Move == 'R') console.log("\t" + player1.name, "Wins!\n");
			break;
	}
}

/* Main function that uses inheritance */
function inheritance() {
	HistoryRepeatsItselfPlayer.prototype = Object.create(Player.prototype);

	var p1 = new Player("Normal Nancy");
	var p2 = new HistoryRepeatsItselfPlayer("Timely Tabitha");

	var p1Move;
	var p2Move;

	// Round one
	console.log("\tRound one...\n");
	console.log("\t" + p1.name);
	console.log("\t---------");
	p1Move = p1.move();
	console.log();

	console.log("\t" + p2.name);
	console.log("\t---------");
	p2Move = p2.move();
	console.log();

	p2.recordOpponentMove(p1Move);
	p1.recordOpponentMove(p2Move);

	whoWins(p1, p2);

	// Round two
	console.log("\tRound two...\n");
	console.log("\t" + p1.name);
	console.log("\t---------");
	p1Move = p1.move();
	console.log();

	console.log("\t" + p2.name);
	console.log("\t---------");
	p2Move = p2.move();
	console.log();

	p2.recordOpponentMove(p1Move);
	p1.recordOpponentMove(p2Move);

	whoWins(p1, p2);
}

/* Player that has a strategy object */
function StrategyPlayer(name, strategy) {
	this.name = name;
	this.strategy = strategy;
	this.previousMoves = [];
	this.oppPreviousMoves = [];

	this.recordOpponentMove = function recordOpponentMove(opponentMove) {
		this.oppPreviousMoves.push(opponentMove);
	}
}

/* Strategy that looks a previous strategy to make current move */
function LookAtPreviousMoveStrategy() {

	/* Makes a smart move */
	LookAtPreviousMoveStrategy.prototype.move = function move(array) {
		var nextMove = this.getNextMove(array);
		return nextMove;
	}

	/* Plays depending on the player's last move */
	LookAtPreviousMoveStrategy.prototype.getNextMove = function getNextMove(array) {
		var length = array.length;
		if (length > 0) {
			var oppLastMove = array[length - 1];
			switch (oppLastMove) {
				case 'R':
					return this.playSmart('R', 'P');
				case 'P': 
					return this.playSmart('P', 'S');
				case 'S': 
					return this.playSmart('S', 'R');
			}
		} else { // Pick random
			var moves = ['R', 'P', 'S'];
			var random = parseInt(Math.floor((Math.random() * 3)));
			return moves[random];
		}
	}

	/* Print out the move and return it */
	LookAtPreviousMoveStrategy.prototype.playSmart = function playSmart(oppLastMove, smartNextMove) {
		console.log("\tIf her last opponent's move was", oppLastMove, "she'll play:", smartNextMove);
		return smartNextMove;
	}	
}

/* Main function for composition */
function composition() {
	var currentMove;
	var player = new StrategyPlayer("Tabitha", new LookAtPreviousMoveStrategy());
	console.log("\t" + player.name);
	console.log("\t--------");

	currentMove = player.strategy.move(player.oppPreviousMoves);
	console.log("\tTabitha's first move should be random:", currentMove);
	player.previousMoves.push(currentMove);
	player.oppPreviousMoves.push('R');

	currentMove = player.strategy.move(player.oppPreviousMoves);
	player.previousMoves.push(currentMove);
	player.oppPreviousMoves.push('P');

	currentMove = player.strategy.move(player.oppPreviousMoves);
	player.previousMoves.push(currentMove);

	console.log("\tAll of Tabitha's moves so far:", player.previousMoves);
	console.log("\tAll of her opponent's moves so far: ['R', 'P']");
}

/* Do work... */
console.log("Inheritance...\n");
inheritance();
console.log("Composition...\n");
composition();
