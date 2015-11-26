'use strict';

angular.module('subutai.plugins.cassandra.service',[])
	.factory('cassandraSrv', cassandraSrv);

cassandraSrv.$inject = ['$http', 'environmentService'];

function cassandraSrv($http, environmentService) {

	var BASE_URL = SERVER_URL + 'rest/cassandra/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var cassandraSrv = {
		getClusters: getClusters,
		createCassandra: createCassandra,
		changeClusterScaling: changeClusterScaling,
		deleteCluster: deleteCluster,
		addNode: addNode,
		deleteNode: deleteNode,
		startNodes: startNodes,
		stopNodes: stopNodes,
		getEnvironments: getEnvironments
	};

	return cassandraSrv;

	function addNode(clusterName) {
		return $http.post(CLUSTER_URL + clusterName + '/add');
	}

	function startNodes(clusterName, nodesArray) {
		var postData = 'clusterName=' + clusterName + '&lxcHosts=' + nodesArray;
		return $http.post(
			CLUSTER_URL + 'nodes/start',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function stopNodes(clusterName, nodesArray) {
		var postData = 'clusterName=' + clusterName + '&lxcHosts=' + nodesArray;
		return $http.post(
			CLUSTER_URL + 'nodes/stop',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function changeClusterScaling(clusterName, scale) {
		return $http.post(CLUSTER_URL + clusterName + '/auto_scale/' + scale);
	}

	function deleteCluster(clusterName) {
		return $http.delete(CLUSTER_URL + clusterName);
	}

	function deleteNode(clusterName, nodeId) {
		return $http.delete(CLUSTER_URL + clusterName + '/node/' + nodeId);
	}

	function getEnvironments() {
		return environmentService.getEnvironments();
	}

	function getClusters(clusterName) {
		if(clusterName === undefined || clusterName === null) clusterName = '';
		return $http.get(
			CLUSTER_URL + clusterName,
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function createCassandra(cassandraJson) {
		var postData = 'clusterConfJson=' + cassandraJson;
		return $http.post(
			CLUSTER_URL + 'create',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
