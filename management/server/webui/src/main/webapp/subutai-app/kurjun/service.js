'use strict';

angular.module('subutai.kurjun.service', [])
	.factory('kurjunService', kurjunService);


kurjunService.$inject = ['$http'];

function kurjunService($http) {

	var KURJUN_URL = SERVER_URL + 'rest/ui/kurjun/';

	var kurjunService = {
		getTemplates: getTemplates
	};

	return kurjunService;

	//// Implementation

	function getTemplates() {
		return $http.get(TEMPLATES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

}
