'use strict'
var PlayerServer = require('./player_server.js');
var GameServer = require('./game_server.js');

/**
 * Constructor for the class. Needs to initialise the players server and game
 * server, create the list of colours and initialise a game id
 * 
 * @constructor
 */
function Server() {
	this.game_id = -1;
	this.colours = [ 'Black', 'Blue', 'Green', 'Red', 'White', 'Yellow' ];
	this.player_server = new PlayerServer();
	this.game_server = new GameServer();
}

/**
 * Function that will start up the server. It should set the game_server and the
 * player_server to listen on the correct ports. It should also connect up the
 * events emitted by the player server so that the correct information is passed
 * onto the game server
 * 
 * @param player_port
 * @param game_port
 */
Server.prototype.start = function(player_port, game_port) {
	var self = this;
	this.game_server.listen(game_port);
	this.game_server.on('initialised', set_game_id);
	this.player_server.listen(player_port);
	this.player_server.on('register', register_player);
	this.player_server.on('move', send_move);
	function set_game_id(game_id) {
		self.game_id = game_id;
	}
	function register_player(player, student_id) {
		var colour = self.getNextColour();
		self.game_server.addPlayer(player, colour, self.game_id);
	}
	function send_move(player, move) {
		self.game_server.makeMove(player, move);
	}
}

/**
 * Function to close down the player server and the game server
 */
Server.prototype.close = function() {
	this.player_server.close();
	this.game_server.close();
}

/**
 * Function to retrieve the id of the game being played
 * 
 * @returns {number|*}
 */
Server.prototype.gameId = function() {
	return this.game_id;
}

/**
 * Function to extract the next colour out of the arrays of colours When a
 * colour is extracted, that colour is removed from the list
 * 
 * @returns {*}
 */
Server.prototype.getNextColour = function() {
	var colour = this.colours.shift();
	return colour;
}

module.exports = Server;
