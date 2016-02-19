'use strict';

liveTrackerSrv.$inject = ['$http'];

function liveTrackerSrv($http) {
	var BASE_URL = SERVER_URL + 'rest/ui/tracker/';
	var OPERATIONS_URL = BASE_URL + 'operations/';
	var SOURCES_URL = OPERATIONS_URL + 'sources';

	var trackerSrv = {
		getModules: getModules,
		getOperations: getOperations,
		getOperation: getOperation,

		getBaseUrl : function() { return BASE_URL; }
	};

	return trackerSrv;

	function getModules() {
		return $http.get(SOURCES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getOperations(module, dateFrom, dateTo, limit) {
		var $url = OPERATIONS_URL + module + '/' + dateFrom + '/' + dateTo + '/' + limit;
		return $http.get($url, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getOperation(module, id) {
		var $url = OPERATIONS_URL + module + '/' + id;
		return $http.get($url, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}	

}
