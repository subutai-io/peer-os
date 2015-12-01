'use strict';

angular.module('subutai.monitoring.service', [])
	.factory('monitoringSrv', monitoringSrv);


monitoringSrv.$inject = ['$http'];

function monitoringSrv($http) {
	var BASE_URL = SERVER_URL + 'rest/ui/monitoring/';

	var monitoringSrv = {
		getInfo: getInfo,
	};

	return monitoringSrv;

	//// Implementation

	
	function getInfo() {
		return $http.get(
			'http://172.16.193.90:8080/rest/ui/metrics/1d2a97cd-7035-4c3f-b9c2-370f2acf417d/E749FE5E18EF03B354959B45D95F234539E65674/12', 
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

}
