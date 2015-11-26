'use strict';

angular.module('subutai.plugins.hadoop.service',[])
	.factory('hadoopSrv', hadoopSrv);

hadoopSrv.$inject = ['$http', 'environmentService'];
function hadoopSrv($http, environmentService) {

	var BASE_URL = SERVER_URL + 'rest/hadoop/';
	var CLUSTER_URL = BASE_URL + 'clusters/';
	var HADOOP_CREATE_URL = BASE_URL + 'configure_environment';

	var hadoopSrv = {
		createHadoop: createHadoop,
		getClusters: getClusters,
		getEnvironments: getEnvironments
	};

	return hadoopSrv;

	function getClusters(clusterName) {
		if(clusterName === undefined || clusterName === null) clusterName = '';
		return $http.get(
			CLUSTER_URL + clusterName,
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function getEnvironments() {
		return environmentService.getEnvironments();
	}

	function createHadoop(hadoopJson) {
		var postData = 'config=' + hadoopJson;
		return $http.post(
			HADOOP_CREATE_URL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
