'use strict';

angular.module('subutai.plugins.elastic-search.service',[])
	.factory('elasticSearchSrv', elasticSearchSrv);

elasticSearchSrv.$inject = ['$http', 'environmentService'];

function elasticSearchSrv($http, environmentService) {
	var BASE_URL = SERVER_URL + 'rest/elasticsearch/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var elasticSearchSrv = {
		getEnvironments: getEnvironments,
		createElasticSearch: createElasticSearch,
		getClusters: getClusters,
		deleteNode: deleteNode,
		addNode: addNode,
		deleteCluster: deleteCluster,
		changeClusterScaling: changeClusterScaling,
		startNodes: startNodes,
		stopNodes: stopNodes,
	};

	return elasticSearchSrv;

	function addNode(clusterName) {
		return $http.post(CLUSTER_URL + clusterName + '/add/');
	}

	function getEnvironments() {
		return environmentService.getEnvironments();
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

	function getClusters(clusterName) {
		if(clusterName === undefined || clusterName === null) clusterName = '';
		return $http.get(
			CLUSTER_URL + clusterName,
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function changeClusterScaling(clusterName, scale) {
		return $http.post(CLUSTER_URL + clusterName + '/auto_scale/' + scale);
	}

	function deleteCluster(clusterName) {
		return $http.delete(CLUSTER_URL + 'remove/' + clusterName);
	}

	function deleteNode(clusterName, nodeId) {
		return $http.delete(CLUSTER_URL + clusterName + '/remove/node/' + nodeId);
	}

	function createElasticSearch(elasticSearchObj) {
		console.log(elasticSearchObj);
		var postData = 'clusterName=' + elasticSearchObj.clusterName + '&environmentId=' + elasticSearchObj.environmentId + '&nodes=' + JSON.stringify(elasticSearchObj.nodes);
		return $http.post(
			CLUSTER_URL + 'install',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
