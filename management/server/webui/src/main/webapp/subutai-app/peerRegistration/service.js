'use strict';

angular.module('subutai.peer-registration.service', [])
	.factory('peerRegistrationService', peerRegistrationService);


peerRegistrationService.$inject = ['$http'];

function peerRegistrationService($http) {
	var PEERS_URL = SERVER_URL + 'rest/ui/peers/';
	var RH_URL = PEERS_URL + 'resource_hosts/';

	var peerRegistrationService = {
		getRequestedPeers: getRequestedPeers,
		registerRequest: registerRequest,
		rejectPeerRequest: rejectPeerRequest,
		approvePeerRequest: approvePeerRequest,
		cancelPeerRequest: cancelPeerRequest,
		unregisterPeerRequest: unregisterPeerRequest,

		getResourceHosts: getResourceHosts,


		getPeersUrl : function() { return PEERS_URL; }
	};

	return peerRegistrationService;

	//// Implementation

	function getRequestedPeers() {
		return $http.get(PEERS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function registerRequest(postData) {
		return $http.post(
			PEERS_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function rejectPeerRequest(peerId) {
		var postData = 'peerId=' + peerId;
		return $http.put(
			PEERS_URL + 'reject/',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

	function unregisterPeerRequest(peerId) {
		var postData = 'peerId=' + peerId;
		return $http.put(
			PEERS_URL + 'unregister/',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

	function cancelPeerRequest(peerId) {
		var postData = 'peerId=' + peerId;
		return $http.put(
			PEERS_URL + 'cancel/',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

	function approvePeerRequest(peerId, keyPhrase) {
		var postData = 'peerId=' + peerId + '&key_phrase=' + keyPhrase;
		return $http.put(
			PEERS_URL + 'approve/',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}


	function getResourceHosts() {
		return $http.get(RH_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}
}
