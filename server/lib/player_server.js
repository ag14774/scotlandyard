'use strict'
var net = require('net');
var events = require('events');
var util = require('util');

function PlayerServer() {
	var self = this;
	this.server = net.createServer(function(player) {
		player.on('data', get);
		function get(data) {
			var message = JSON.parse(data);
			self.onInput(player, message);
		}
	});
}
util.inherits(PlayerServer, events.EventEmitter);

/**
 * Function to parse the input data
 * 
 * @param player
 *            Socket that represents the player
 * @param Data
 *            parsed as a JSON object
 */
PlayerServer.prototype.onInput = function(player, data) {
	if (data.type == 'REGISTER') {
		// {type: "REGISTER", student_id: "testId"}
		this.emit('register', player, data.student_id);
	} else if (data.type == 'MOVE') {
		// {type:"MOVE", move { target:5, colour: "Red", ticket: "Bus"}}
		this.emit('move',player, data);
	}
}

PlayerServer.prototype.listen = function(port) {
	this.server.listen(port);
}

PlayerServer.prototype.close = function() {
	this.server.close();
}

module.exports = PlayerServer;