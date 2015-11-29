'use strict';

angular.module('subutai.plugins.lucene.service',[])
	.factory('luceneSrv', luceneSrv);

luceneSrv.$inject = ['$http', 'hadoopSrv'];

function luceneSrv($http, hadoopSrv) {
	var BASE_URL = SERVER_URL + 'rest/lucene/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var luceneSrv = {
		getHadoopClusters: getHadoopClusters,
		createLucene: createLucene,
		getClusters: getClusters,
		deleteNode: deleteNode,
		getAvailableNodes: getAvailableNodes,
		addNode: addNode,
		deleteCluster: deleteCluster,
	};

	return luceneSrv;

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

	function createLucene(luceneObj) {
		console.log(luceneObj);
		var postData = 'clusterName=' + luceneObj.clusterName + '&hadoopClusterName=' + luceneObj.hadoopClusterName + '&nodes=' + JSON.stringify(luceneObj.nodes);
		return $http.post(
			BASE_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
