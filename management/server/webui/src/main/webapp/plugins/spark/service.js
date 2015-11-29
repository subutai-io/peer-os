'use strict';

angular.module('subutai.plugins.spark.service',[])
	.factory('sparkSrv', sparkSrv);

sparkSrv.$inject = ['$http', 'hadoopSrv'];


function sparkSrv($http, hadoopSrv) {
	var BASE_URL = SERVER_URL + 'rest/spark/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var sparkSrv = {
		getHadoopClusters: getHadoopClusters,
		createSpark: createSpark,
		getClusters: getClusters,
		deleteNode: deleteNode,
		getAvailableNodes: getAvailableNodes,
		addNode: addNode,
		deleteCluster: deleteCluster,
		startMasterNode: startMasterNode,
		stopMasterNode: stopMasterNode,
		startNodes: startNodes,
		stopNodes: stopNodes
	};

	return sparkSrv;

	function addNode(clusterName, lxcHostname) {
		return $http.post(CLUSTER_URL + clusterName + '/add/node/' + lxcHostname);
	}

	function getHadoopClusters(clusterName) {
		return hadoopSrv.getClusters(clusterName);
	}

	function getClusters(clusterName) {
		if(clusterName === undefined || clusterName === null) clusterName = '';
		return $http.get(
			CLUSTER_URL + clusterName,
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function getAvailableNodes(clusterName) {
		return $http.get(
			CLUSTER_URL + clusterName + '/available/nodes',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function deleteCluster(clusterName) {
		return $http.delete(CLUSTER_URL + 'destroy/' + clusterName);
	}

	function deleteNode(clusterName, nodeId) {
		return $http.delete(CLUSTER_URL + clusterName + '/destroy/node/' + nodeId);
	}

	function createSpark(sparkObj) {
		var postData = 'clusterName=' + sparkObj.clusterName 
			+ '&hadoopClusterName=' + sparkObj.hadoopClusterName 
			+ '&master=' + sparkObj.server 
			+ "&slaves=" + JSON.stringify (sparkObj.nodes);
		return $http.post(
			BASE_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function startMasterNode(clusterName, lxcHostName) {
		return $http.put (CLUSTER_URL + clusterName + "/start/node/" + lxcHostName + '/master/true');
	}

	function stopMasterNode(clusterName, lxcHostName) {
		return $http.put (CLUSTER_URL + clusterName + "/stop/node/" + lxcHostName + '/master/true');
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
}
