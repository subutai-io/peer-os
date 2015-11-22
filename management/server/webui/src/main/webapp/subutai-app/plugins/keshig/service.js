'use strict';

angular.module('subutai.plugins.keshig.service',[])
	.factory('keshigSrv', keshigSrv);

keshigSrv.$inject = ['$http'];

function keshigSrv($http) {

	var baseURL = serverUrl + 'keshig/';
	var keshigCreateURL = baseURL + 'clusters/create';
	var environmentsURL = serverUrl + 'environments_ui/';	

	var keshigSrv = {
		getKeshig: getKeshig,
		createKeshig: createKeshig,
		getEnvironments: getEnvironments
	};

	return keshigSrv;

	function getEnvironments() {
		return $http.get(environmentsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getKeshig() {
		return $http.get(baseURL);
	}

	function createKeshig(keshigJson) {
		var postData = 'clusterConfJson=' + keshigJson;
		return $http.post(
			keshigCreateURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
