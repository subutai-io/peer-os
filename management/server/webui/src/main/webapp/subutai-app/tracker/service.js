'use strict';

angular.module('subutai.tracker.service', [])
	.factory('trackerSrv', trackerSrv);

trackerSrv.$inject = ['$http'];

function trackerSrv($http) {
	var BASE_URL = SERVER_URL + 'rest/ui/tracker/';
	var OPERATIONS_URL = BASE_URL + 'operations/';
	var SOURCES_URL = OPERATIONS_URL + 'sources';
	var NOTIFICATIONS_URL = BASE_URL + 'notifications';

	var trackerSrv = {
		getModules: getModules,
		getOperations: getOperations,
		getOperation: getOperation,

		getDownloadProgress: getDownloadProgress,

		getNotifications: getNotifications,
		deleteNotification: deleteNotification,
		deleteAllNotifications: deleteAllNotifications,

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


	function getDownloadProgress(id) {
		return $http.get(SERVER_URL + "rest/ui/environments/" + id + "/download");
	}


	function getNotifications() {
		return $http.get(NOTIFICATIONS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function deleteNotification(source, uuid) {
		return $http.delete(NOTIFICATIONS_URL + source + '/' + uuid);
	}

	function deleteAllNotifications() {
		return $http.delete(NOTIFICATIONS_URL);
	}

}
