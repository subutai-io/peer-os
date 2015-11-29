'use strict';

angular.module('subutai.plugins.nutch.service',[])
	.factory('nutchSrv', nutchSrv);

nutchSrv.$inject = ['$http', 'hadoopSrv'];

function nutchSrv($http, hadoopSrv) {
	var BASE_URL = SERVER_URL + 'rest/nutch/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var nutchSrv = {
		getHadoopClusters: getHadoopClusters,
		createNutch: createNutch,
		getClusters: getClusters,
		deleteNode: deleteNode,
		getAvailableNodes: getAvailableNodes,
		addNode: addNode,
		deleteCluster: deleteCluster,
	};

	return nutchSrv;

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

	function createNutch(nutchObj) {
		console.log(nutchObj);
		var postData = 'clusterName=' + nutchObj.clusterName + '&hadoopClusterName=' + nutchObj.hadoopClusterName + '&nodes=' + JSON.stringify(nutchObj.nodes);
		return $http.post(
			BASE_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
