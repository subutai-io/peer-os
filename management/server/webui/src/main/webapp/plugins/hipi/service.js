'use strict';

angular.module('subutai.plugins.hipi.service',[])
	.factory('hipiSrv', hipiSrv);

hipiSrv.$inject = ['$http', 'hadoopSrv'];

function hipiSrv($http, hadoopSrv) {
	var BASE_URL = SERVER_URL + 'rest/hipi/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var hipiSrv = {
		getHadoopClusters: getHadoopClusters,
		createHipi: createHipi,
		getClusters: getClusters,
		deleteNode: deleteNode,
		getAvailableNodes: getAvailableNodes,
		addNode: addNode,
		deleteCluster: deleteCluster,
	};

	return hipiSrv;

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

	function createHipi(hipiObj) {
		console.log(hipiObj);
		var postData = 'clusterName=' + hipiObj.clusterName + '&hadoopClusterName=' + hipiObj.hadoopClusterName + '&nodes=' + JSON.stringify(hipiObj.nodes);
		return $http.post(
			BASE_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
