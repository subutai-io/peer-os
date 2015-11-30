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
		changeClusterScaling: changeClusterScaling,
		deleteCluster: deleteCluster,
		addNode: addNode,
		startNode: startNode,
		stopNode: stopNode,
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

	function addNode(clusterName) {
		return $http.post(CLUSTER_URL + clusterName + '/nodes');
	}

	function startNode(clusterName, nodeType) {
		var postData = '';
		return $http.put(
			CLUSTER_URL + nodeType + clusterName + '/start',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function stopNode(clusterName, nodeType) {
		var postData = '';
		return $http.put(
			CLUSTER_URL + nodeType + clusterName + '/stop',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function changeClusterScaling(clusterName, scale) {
		return $http.post(CLUSTER_URL + clusterName + '/auto_scale/' + scale);
	}

	function getEnvironments() {
		return environmentService.getEnvironments();
	}

	function deleteCluster(clusterName) {
		return $http.delete(CLUSTER_URL + clusterName);
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
