'use strict';

angular.module('subutai.monitoring.service', [])
	.factory('monitoringSrv', monitoringSrv);


monitoringSrv.$inject = ['$http', 'environmentService', 'peerRegistrationService'];

function monitoringSrv($http, environmentService, peerRegistrationService) {
	var BASE_URL = SERVER_URL + 'rest/ui/metrics/';

	var monitoringSrv = {
		getEnvironments: getEnvironments,
		getResourceHosts: getResourceHosts,
		getInfo: getInfo,
	};

	return monitoringSrv;

	function getEnvironments() {
		return environmentService.getEnvironments();
	}

	function getResourceHosts() {
		return peerRegistrationService.getResourceHosts();
	}

	function getInfo(environmentId, hostId, period) {
		var url = BASE_URL;
		if(environmentId !== undefined && environmentId.length > 0) {
			url += environmentId + '/';
		}
		if(hostId !== undefined && hostId.length > 0) {
			url += hostId + '/';
		}
		return $http.get(
			url + period, 
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

}
