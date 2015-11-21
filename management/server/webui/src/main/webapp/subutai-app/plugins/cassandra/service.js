'use strict';

angular.module('subutai.plugins.cassandra.service',[])
	.factory('cassandraSrv', cassandraSrv);

cassandraSrv.$inject = ['$http'];

function cassandraSrv($http) {

	var baseURL = serverUrl + 'cassandra/';
	var clustersURL = baseURL + 'clusters/';
	var environmentsURL = serverUrl + 'environments_ui/';	

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
		return $http.post(clustersURL + clusterName + '/add');
	}

	function startNodes(clusterName, nodesArray) {
		var postData = 'clusterName=' + clusterName + '&lxcHosts=' + nodesArray;
		return $http.post(
			clustersURL + 'nodes/start', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function stopNodes(clusterName, nodesArray) {
		var postData = 'clusterName=' + clusterName + '&lxcHosts=' + nodesArray;
		return $http.post(
			clustersURL + 'nodes/stop', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function changeClusterScaling(clusterName, scale) {
		return $http.post(clustersURL + clusterName + '/auto_scale/' + scale);
	}

	function deleteCluster(clusterName) {
		return $http.delete(clustersURL + clusterName);
	}

	function deleteNode(clusterName, nodeId) {
		return $http.delete(clustersURL + clusterName + '/node/' + nodeId);
	}

	function getEnvironments() {
		return $http.get(environmentsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getClusters(clusterName) {
		if(clusterName === undefined || clusterName === null) clusterName = '';
		return $http.get(
			clustersURL + clusterName,
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function createCassandra(cassandraJson) {
		var postData = 'clusterConfJson=' + cassandraJson;
		return $http.post(
			clustersURL + 'create', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
