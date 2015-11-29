'use strict';

angular.module('subutai.plugins.storm.service',[])
	.factory('stormSrv', stormSrv);

stormSrv.$inject = ['$http', 'environmentService'];


function stormSrv($http, environmentService) {
	var BASE_URL = SERVER_URL + 'rest/storm/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var stormSrv = {
		getEnvironments: getEnvironments,
		createStorm: createStorm,
		getClusters: getClusters,
		deleteNode: deleteNode,
		getAvailableNodes: getAvailableNodes,
		addNode: addNode,
		deleteCluster: deleteCluster,
		startNode: startNode,
		stopNode: stopNode,
		changeClusterScaling: changeClusterScaling,
		startNodes: startNodes,
		stopNodes: stopNodes
	};

	return stormSrv;

	function addNode(clusterName) {
		return $http.post(CLUSTER_URL + clusterName + '/add/');
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

	function createStorm(stormObj) {
		var postData = 'clusterName=' + stormObj.clusterName 
			+ '&environmentId=' + stormObj.environmentId
			+ '&nimbus=' + stormObj.server
			+ "&supervisors=" + JSON.stringify (stormObj.nodes);
		console.log (stormObj);
		console.log (postData);
		return $http.post(
			CLUSTER_URL + 'install',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function startNode (clusterName, lxcHostName) {
		return $http.put (CLUSTER_URL + clusterName + "/start/node/" + lxcHostName);
	}

	function stopNode (clusterName, lxcHostName) {
		return $http.put (CLUSTER_URL + clusterName + "/stop/node/" + lxcHostName);
	}

	function changeClusterScaling (clusterName, val) {
		return $http.post (CLUSTER_URL + clusterName + "/auto_scale/" + val);
	}

	function startNodes(clusterName, nodesArray) {
		var postData = 'clusterName=' + clusterName + '&lxcHostNames=' + nodesArray;
		return $http.post(
			CLUSTER_URL + 'nodes/start',
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function stopNodes(clusterName, nodesArray) {
		var postData = 'clusterName=' + clusterName + '&lxcHostNames=' + nodesArray;
		console.log (postData);
		return $http.post(
			CLUSTER_URL + 'nodes/stop',
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
