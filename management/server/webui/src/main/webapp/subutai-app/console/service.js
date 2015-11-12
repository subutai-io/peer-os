'use strict';

angular.module('subutai.console.service', [])
	.factory('consoleService', consoleService);

consoleService.$inject = ['$http'];

function consoleService($http) {
	var baseURL = 'http://172.16.131.205:8181/rest/';
	var resourceHostsURL = baseURL + 'command_ui/resource_hosts/';
	var environmentsURL = baseURL + 'environments_ui/';	

	var consoleService = {
		getResourceHosts: getResourceHosts,
		getEnvironments: getEnvironments,
	};

	return consoleService;

	// Implementation

	function getResourceHosts() {
		return $http.get(resourceHostsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getEnvironments() {
		return $http.get(environmentsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}
}
