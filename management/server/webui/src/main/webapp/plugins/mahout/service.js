'use strict';

angular.module('subutai.plugins.mahout.service',[])
	.factory('mahoutSrv', mahoutSrv);

mahoutSrv.$inject = ['$http', 'hadoopSrv'];

function mahoutSrv($http, hadoopSrv) {
	var BASE_URL = SERVER_URL + 'rest/mahout/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var mahoutSrv = {
		getHadoopClusters: getHadoopClusters,
		createMahout: createMahout,
		getClusters: getClusters,
		deleteNode: deleteNode,
		getAvailableNodes: getAvailableNodes,
		addNode: addNode,
		deleteCluster: deleteCluster,
	};

	return mahoutSrv;

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
		return $http.delete(CLUSTER_URL + 'remove/' + clusterName);
	}

	function deleteNode(clusterName, nodeId) {
		return $http.delete(CLUSTER_URL + clusterName + '/remove/node/' + nodeId);
	}

	function createMahout(mahoutObj) {
		console.log(mahoutObj);
		var postData = 'clusterName=' + mahoutObj.clusterName + '&hadoopClusterName=' + mahoutObj.hadoopClusterName + '&nodes=' + JSON.stringify(mahoutObj.nodes);
		return $http.post(
			BASE_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
