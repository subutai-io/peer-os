'use strict';

angular.module('subutai.plugins.hadoop.service',[])
	.factory('hadoopSrv', hadoopSrv);

hadoopSrv.$inject = ['$http'];
function hadoopSrv($http) {

	var baseURL = serverUrl + 'hadoop/';
	var hadoopCreateURL = baseURL + 'clusters/create';
	var environmentsURL = serverUrl + 'environments_ui/';		

	var hadoopSrv = {
		getEnvironments: getEnvironments,
		getHadoop: getHadoop
	};

	return hadoopSrv;

	function getHadoop() {
		return $http.get(hadoopUrl);
	}

	function getEnvironments() {
		return $http.get(environmentsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function createHadoop(hadoopJson) {
		var postData = 'clusterConfJson=' + hadoopJson;
		return $http.post(
			hadoopCreateURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
