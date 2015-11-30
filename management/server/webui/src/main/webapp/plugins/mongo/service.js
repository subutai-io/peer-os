'use strict';

angular.module('subutai.plugins.mongo.service',[])
	.factory('mongoSrv', mongoSrv);

mongoSrv.$inject = ['$http', 'environmentService'];

function mongoSrv($http, environmentService) {

	var BASE_URL = SERVER_URL + 'rest/mongodb/';
	var CLUSTER_URL = BASE_URL + 'clusters/';

	var mongoSrv = {
		listClusters: listClusters,
		configureCluster: configureCluster,
		destroyCluster: destroyCluster,
		startNode: startNode,
		stopNode: stopNode,
		startCluster: startCluster,
		stopCluster: stopCluster,
		destroyNode: destroyNode,
		checkNode: checkNode,
		addNode: addNode,
		getEnvironments: getEnvironments,
		createMongo: createMongo,
		startNodes: startNodes,
		stopNodes: stopNodes,
		sendRouter: sendRouter,
		sendDataNode: sendDataNode,
		changeClusterScaling: changeClusterScaling
	};

	return mongoSrv;


	function listClusters (clusterName) {
		if (clusterName === undefined || clusterName === null) {
			clusterName = "";
		}
		return $http.get (CLUSTER_URL + clusterName, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}


	function configureCluster (config) {
		return $http.post (BASE_URL + "configure_environment", config);
	}

	function destroyCluster (clusterName) {
		return $http.delete (CLUSTER_URL + "destroy/" + clusterName);
	}


	function startNode (clusterName, lxcHostName, nodeType) {
		return $http.put (CLUSTER_URL + clusterName + "/start/node/" + lxcHostName + "/nodeType/" + nodeType);
	}

	function startNodes(clusterName, nodesArray) {
		var postData = 'clusterName=' + clusterName + '&lxcHostNames=' + nodesArray;
		return $http.post(
			CLUSTER_URL + 'nodes/start',
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}



	function stopNode (clusterName, lxcHostName, nodeType) {
    	return $http.put (CLUSTER_URL + clusterName + "/stop/node/" + lxcHostName + "/nodeType/" + nodeType);
    }


	function stopNodes(clusterName, nodesArray) {
		var postData = 'clusterName=' + clusterName + '&lxcHostNames=' + nodesArray;
		return $http.post(
			CLUSTER_URL + 'nodes/stop',
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}


	function startCluster (clusterName) {
		return $http.put (CLUSTER_URL + clusterName + "/start");
	}


	function stopCluster (clusterName) {
    	return $http.put (CLUSTER_URL + clusterName + "/stop");
    }


	function destroyNode (clusterName, lxcHostName, nodeType) {
		return $http.delete (CLUSTER_URL + clusterName + "/destroy/node/" + lxcHostName + "/nodeType/" + nodeType);
	}


	function checkNode (clusterName, lxcHostName, nodeType) {
		return $http.get (CLUSTER_URL + clusterName + "/check/node/" + lxcHostName + "/nodeType/" + nodeType);
	}

	function addNode (clusterName, nodeType) {
		return $http.post (CLUSTER_URL + clusterName + "/add/node/nodeType/" + nodeType);
	}

	function getEnvironments() {
		return environmentService.getEnvironments();
	}

	function changeClusterScaling(clusterName, scale) {
		console.log(scale);
		return $http.post(CLUSTER_URL + clusterName + '/auto_scale/' + scale);
	}

	function createMongo (mongoJson) {
		var postData = 'clusterConfJson=' + mongoJson;
		return $http.post(
			CLUSTER_URL + 'create',
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function sendRouter(clusterName) {
		return mongoSrv.addNode (clusterName, "router");
	}

	function sendDataNode(clusterName) {
		return mongoSrv.addNode (clusterName, "data");
	}
}
