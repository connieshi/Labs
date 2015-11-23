// First click to play game
var play = document.getElementById('startButton');
play.addEventListener('click', handleClick);

// Game constructor for game states
function Game(numSymbols) {
	this.charSet = ['♨', '❤', '❄', '☯', '☂', '✂', '☁', '✈'];
	this.squares = [1, 4, 9, 16];
	this.lastClicked = null;
	this.foundMatches = 0;
	this.clicks = 0;
	this.numSymbols = numSymbols;
}

// Handles click of play game button by parsing number of symbols to include
function handleClick(event) {
	var numSymbols = parseInt(document.getElementById('numSymbols').value);
	if (numSymbols > 8) {
		numSymbols = 8;
	} else if (numSymbols <= 0) {
		numSymbols = 1;
	}

	var game = new Game(numSymbols);
	document.getElementById('game').removeChild(document.getElementById('startForm'));
	displayBoard(game);
}

// Pick the first numSymbols pairs to put in array and shuffle
function getUseSymbols(game) {
	var symbols = [];
	for (var i = 0; i < game.numSymbols; i++) {
		symbols.push(game.charSet[i]);
		symbols.push(game.charSet[i]);
	}

	for (var j = 0; j < symbols.length; j++) {
		var random = Math.floor(Math.random() * symbols.length);
		var temp = symbols[j];
		symbols[j] = symbols[random];
		symbols[random] = temp;
	}
	return symbols;
}

// Create the board to play on
function displayBoard(game) {
	// Callback function for event listener
	var showSymbol = function showSymbol(event) {
		this.childNodes[0].style.visibility = 'visible';
		game.clicks++;

		// If there's already a card picked from previous click
		if (game.lastClicked) {
			// If the two cards picked match
			if (game.lastClicked.childNodes[0].textContent === this.childNodes[0].textContent) {
				game.foundMatches++;
				game.lastClicked.removeEventListener('click', showSymbol);
				this.removeEventListener('click', showSymbol);
				game.lastClicked = null;
			} else {
				var currentNode = this.childNodes[0];
				setTimeout(function() {
					game.lastClicked.childNodes[0].style.visibility = 'hidden';
					game.lastClicked.addEventListener('click', showSymbol);
					currentNode.style.visibility = 'hidden';
					game.lastClicked = null;
				}, 200);
			}

			// If all cards have found their matches
			if (game.foundMatches === game.numSymbols) {
				setTimeout(function() {
					document.getElementById('board').removeChild(document.getElementById('table'));
					var p = document.createElement('p');
					p.appendChild(document.createTextNode('You are done, thanks for playing!'));
					document.getElementById('game').appendChild(p);
					document.getElementById('guesses').childNodes[0].textContent = 'Number of guesses: ' + game.clicks;
				}, 300);
				return;
			}
		} else { // This is the first card picked
			game.lastClicked = this;
			game.lastClicked.removeEventListener('click', showSymbol);
			game.clicks--; // Handle 1 guess per every two card pairs
		}

		// Update the count of number of guesses
		document.getElementById('guesses').childNodes[0].textContent = 'Number of guesses: ' + game.clicks;
	}

	// Create table with rows and columns
	createTable(game, showSymbol);
	createRowsAndCols(game, showSymbol);
}

// Create the game board and text to show number of guesses
function createTable(game, showSymbol) {
	var newDivElement = document.createElement('div');
	newDivElement.id = 'board';
	document.getElementById('game').appendChild(newDivElement);

	var newTableElement = document.createElement('table');
	newTableElement.id = 'table';
	document.getElementById('board').appendChild(newTableElement);

	var newParagraph = document.createElement('p');
	var textNode = document.createTextNode('Number of guesses: ' + game.clicks);
	newParagraph.appendChild(textNode);
	newParagraph.id = 'guesses';
	document.getElementById('game').appendChild(newParagraph);
}

// Create rows and columns within the table to display the symbols
function createRowsAndCols(game, showSymbol) {
	var symbols = getUseSymbols(game);
	var newTableElement = document.getElementById('table');
	var row, col;

	// Try to make it square, otherwise make 2 rows
	if (game.squares.indexOf(game.numSymbols * 2) != -1) {
		row = Math.sqrt(game.numSymbols * 2);
		col = row;
	} else {
		row = 2;
		col = game.numSymbols;
	}

	// Create rows and columns in table with symbols
	for (var i = 0; i < row; i++) {
		var tr = document.createElement('tr');
		for (var j = 0; j < col; j++) {
			// Create new elements needed
			var td = document.createElement('td');
			var text = document.createTextNode(symbols.pop());
			var span = document.createElement('span');

			// Create parent-child relationships
			span.style.visibility = 'hidden';
			span.classList.add('symbol');
			span.appendChild(text);
			td.appendChild(span);
			td.classList.add('box');
			td.addEventListener('click', showSymbol);
			tr.appendChild(td);
		}
		newTableElement.appendChild(tr);
	}
}
