'use strict';

angular.module('subutai.console.service', [])
	.factory('consoleService', consoleService);

consoleService.$inject = ['$http'];

function consoleService($http) {
	var baseURL = serverUrl + 'command_ui/';
	var resourceHostsURL = baseURL + 'resource_hosts/';
	var environmentsURL = serverUrl + 'environments_ui/';	

	var consoleService = {
		getResourceHosts: getResourceHosts,
		getEnvironments: getEnvironments,
		sendCommand: sendCommand,
	};

	return consoleService;

	// Implementation

	function getResourceHosts() {
		return $http.get(resourceHostsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getEnvironments() {
		return $http.get(environmentsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function sendCommand(cmd, peerId, path) {
		var postData = 'command=' + cmd + '&hostId=' + peerId + '&path=' + path;
		return $http.post(
			baseURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
