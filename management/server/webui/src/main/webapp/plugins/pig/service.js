'use strict';

angular.module('subutai.plugins.pig.service',[])
	.factory('pigSrv', pigSrv);

pigSrv.$inject = ['$http', 'hadoopSrv'];

function pigSrv($http, hadoopSrv) {
	var BASE_URL = SERVER_URL + 'rest/pig/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var pigSrv = {
		getHadoopClusters: getHadoopClusters,
		createPig: createPig,
		getClusters: getClusters,
		deleteNode: deleteNode,
		getAvailableNodes: getAvailableNodes,
		addNode: addNode,
		deleteCluster: deleteCluster,
	};

	return pigSrv;

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

	function createPig(pigObj) {
		var postData = 'clusterName=' + pigObj.clusterName + '&hadoopClusterName=' + pigObj.hadoopClusterName + '&nodes=' + JSON.stringify(pigObj.nodes);
		return $http.post(
			BASE_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
