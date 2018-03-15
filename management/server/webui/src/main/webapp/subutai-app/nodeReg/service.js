'use strict';

angular.module('subutai.nodeReg.service',[])
	.factory('nodeRegSrv', nodeRegSrv);

nodeRegSrv.$inject = ['$http'];


function nodeRegSrv($http) {
	var BASE_URL = SERVER_URL + 'rest/v1/registration/';
	var NODES_URL = BASE_URL + "requests/";
	var UPDATE_URL = SERVER_URL + "rest/v1/peer/hosts/";

	var nodeRegSrv = {
		getData : getData,
		approveReq : approveReq,
		rejectReq : rejectReq,
		updateReq : updateReq,
		removeReq : removeReq,
		unblockReq : unblockReq,
		changeHostName : changeHostName
	};

	return nodeRegSrv;

	function getData() {
		return $http.get(NODES_URL);
	}

	function approveReq(nodeId) {
		return $http.post(NODES_URL + nodeId + '/approve');
	}

	function rejectReq(nodeId) {
		return $http.post(NODES_URL + nodeId + '/reject');
	}

	function updateReq(nodeId) {
		return $http.post(UPDATE_URL + nodeId + '/update');
	}

	function unblockReq(nodeId) {
		return $http.post(NODES_URL + nodeId + '/unblock');
	}

	function removeReq(nodeId) {
		return $http.post(NODES_URL + nodeId + '/remove');
	}

	function changeHostName(rhId, name){
	    return $http.post(BASE_URL + rhId + '/hostname/' + name);
	}
}
