'use strict';

angular.module('subutai.nodeReg.service',[])
	.factory('nodeRegSrv', nodeRegSrv);

nodeRegSrv.$inject = ['$http'];


function nodeRegSrv($http) {
	var BASE_URL = SERVER_URL + 'rest/v1/registration/';
	var NODES_URL = BASE_URL + "requests/";

	var nodeRegSrv = {
		getData : getData,
		approveNode : approveNode
	};

	return nodeRegSrv;

	function getData() {
		return $http.get(NODES_URL);
	}

	function approveNode(nodeId) {
		return $http.post(NODES_URL + nodeId + '/approve');
	}

}
