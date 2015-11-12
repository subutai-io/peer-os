/**
 * Created by ubuntu on 5/14/15.
 */
'use strict';

angular.module('subutai.tracker.service', [])
	.factory('trackerSrv', trackerSrv);

trackerSrv.$inject = ['$http'];

function trackerSrv($http) {
	var baseURL = 'http://172.16.131.205:8181/rest/';
	var modulesURL = baseURL + 'tracker_ui/operations/sources';
	var operationsURL = baseURL + 'tracker_ui/operations/';

	var trackerSrv = {
		getModules: getModules,
		getOperations: getOperations,
		getOperation: getOperation
	};

	return trackerSrv;

	function getModules() {
		return $http.get(modulesURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getOperations(module, dateFrom, dateTo, limit) {
		var $url = operationsURL + module + '/' + dateFrom + '/' + dateTo + '/' + limit;
		return $http.get($url, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getOperation(module, id) {
		var $url = operationsURL + module + '/' + id;
		return $http.get($url, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}	

}
