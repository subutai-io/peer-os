'use strict';

angular.module('subutai.console.service', [])
	.factory('consoleService', consoleService);

consoleService.$inject = ['$http', 'environmentService'];

function consoleService($http, environmentService) {
	var BASE_URL = SERVER_URL + 'rest/ui/commands/';

	var consoleService = {
		getEnvironments: getEnvironments,
		sendCommand: sendCommand,
		getSSH: getSSH,
		getResourceHosts: getResourceHosts
	};

	return consoleService;

	// Implementation

	function getEnvironments() {
		return environmentService.getEnvironments();
	}

	function getResourceHosts(){
	   return environmentService.getResourceHosts();
	}

	function getSSH(environmentId, hostId, period) {
		if(environmentId === undefined || environmentId.length == 0) {
			environmentId = null;
		}
		return $http.get(
			environmentService.getServerUrl() + environmentId + '/containers/' + hostId + '/ssh',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);		
	}

	function sendCommand(cmd, hostId, path, daemon, timeout, environmentId) {
		var postData = 'command=' + encodeURIComponent(cmd) + '&hostid=' + hostId;

		if(daemon) {
			postData += '&daemon=true';
		}
		if(timeout !== undefined && timeout > 0) {
			postData += '&timeout=' + parseInt(timeout);
		}
		if(environmentId && environmentId.length > 0) {
			postData += '&environmentid=' + environmentId;
		}
		if(path && path.length > 0) {
			postData += '&path=' + path;
		}

		return $http.post(
			BASE_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
