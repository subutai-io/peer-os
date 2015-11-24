'use strict';

angular.module('subutai.console.service', [])
	.factory('consoleService', consoleService);

consoleService.$inject = ['$http', 'environmentService'];

function consoleService($http, environmentService) {
	var BASE_URL = SERVER_URL + 'rest/ui/commands/';

	var consoleService = {
		getEnvironments: getEnvironments,
		sendCommand: sendCommand
	};

	return consoleService;

	// Implementation

	function getEnvironments() {
		return environmentService.getEnvironments();
	}

	function sendCommand(cmd, peerId, path) {
		var postData = 'command=' + cmd + '&hostId=' + peerId + '&path=' + path;
		return $http.post(
			BASE_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
