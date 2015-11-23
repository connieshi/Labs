var readlineSync = require('readline-sync');
var suits = ["♠", "♥", "♦", "♣"];
var face = ["A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"];
var blackjack = 21;

/*
 * Generates all 52 cards of the deck in order
 */
function generateCards() {
	var deck = [];
	for (var i = 0; i < suits.length; i++) {
		for (var j = 0; j < face.length; j++) {
			deck.push({suit: suits[i], face: face[j]});
		}
	}
	return deck;
}

/*
 * Shuffle all 52 cards by randomly swapping it with another index
 */
function shuffle(deck) {
	for (var i = 0; i < deck.length; i++) {
		var random = Math.floor(Math.random() * (deck.length - i)) + i;
		swap(i, random, deck);
	}
	return deck;
}

/*
 * Swap current location i with random location
 */
function swap(i, random, deck) {
	var temp = deck[i];
	deck[i] = deck[random];
	deck[random] = temp;
}

/*
 * Given an array of cards, calculate the total of the hand, where each
 * face card is 10, A is either 1 or 11, and every number card as its own value.
 */
function calculateHand(playerHand) {
	var total = 0;
	var aceCount = 0;
	for (var i = 0; i < playerHand.length; i++) {
		var current = playerHand[i];
		if (!isNaN(current.face)) {
			total += parseInt(current.face);
		} else {
			if (current.face == "A") {
				aceCount++;
				total += 11;
			} else if (current.face == "J" || current.face == "Q" || current.face == "K") {
				total += 10;
			}
		}
	}

	// If bust and there are aces in play, subtract 10 so A = 1 instead of 11
	while (total > blackjack && aceCount > 0) {
		total -= 10;
		aceCount--;
	}
	return total;
}

/*
 * Given 2 player's totals, determine who wins
 */
function determineWinner(playerTotal, computerTotal) {
	if ((playerTotal > blackjack && computerTotal > blackjack) 
			|| (computerTotal == playerTotal)) {
		return "Tie";
	} else if (playerTotal > blackjack) {
		return "Computer Wins!";
	} else if (computerTotal > blackjack) {
		return "Player Wins!";
	} else {
		return (playerTotal > computerTotal) ? "Player Wins!" : "Computer Wins!";
	}
}

/*
 * Interactive black jack game to deal cards, hit, or stay.
 * Computer must hit until it reaches 17.
 */
function blackjackGame() {
	var deck = shuffle(generateCards());

	while (deck.length >= 26) {
		var playerHand = [];
		var computerHand = [];

		// 2 cards first dealt for both players
		playerHand.push(deck.pop());
		playerHand.push(deck.pop());
		computerHand.push(deck.pop());
		computerHand.push(deck.pop());

		// Player goes first
		var playerResult = calculateHand(playerHand);
		printHand("Your", playerHand, playerResult);
		
		// If player hasn't busted, provide option to hit or stay
		while (playerResult < blackjack) {
			var letter = readlineSync.question('type h to (h)it or s to (s)tay: ');

			if (letter === 'h') {
				playerHand.push(deck.pop());
				playerResult = calculateHand(playerHand);
				printHand("Your", playerHand, playerResult);
				// Hitting caused player to bust
				if (playerResult > blackjack) {
					console.log("BUSTED!");
					break;
				}
			} else if (letter === 's') {
				break;
			}
		}
		
		// Then computer goes, hitting until 17 is reached
		var computerResult = calculateHand(computerHand);
		while (computerResult < 17) {
			computerHand.push(deck.pop());
			computerResult = calculateHand(computerHand);
		}

		// Figure out who won and end the round
		printHand("Computer", computerHand, computerResult);
		console.log(determineWinner(playerResult, computerResult));
		printCardsLeft(deck);
	}
	console.log("Less than 26 cards left. Game over!");
}

/*
 * Print the hands of the player with the calculated result
 */
function printHand(which, playerHand, playerHandResult) {
	var handString = "[";
	for (var i = 0; i < playerHand.length; i++) {
		handString += playerHand[i].face + playerHand[i].suit;
	}
	handString += "]";

	console.log(which + " hand is: " + handString + " ... for a total of " 
			+ playerHandResult);
}

/*
 * Print how many cards are left in the deck
 */
function printCardsLeft(deck) {
	console.log("There are " + deck.length + " cards left in the deck.");
	console.log("-------------------------------------------------------");
}

// Play black jack
blackjackGame();
