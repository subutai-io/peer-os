'use strict';

angular.module('subutai.peer-registration.service', [])
	.factory('peerRegistrationService', peerRegistrationService);


peerRegistrationService.$inject = ['$http'];

function peerRegistrationService($http) {
	var BASE_URL = 'http://172.16.131.205:8181/rest/';
	var peersURL = BASE_URL + 'peer_ui/';

	var peerRegistrationService = {
		getRequestedPeers: getRequestedPeers,
		registerRequest: registerRequest,
		rejectPeerRequest: rejectPeerRequest,
		approvePeerRequest: approvePeerRequest
	};

	return peerRegistrationService;

	//// Implementation

	function getRequestedPeers() {
		return $http.get(peersURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function registerRequest(postData) {
		return $http.post(
			peersURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function rejectPeerRequest(peerId) {
		var postData = 'rejectedPeerId=' + peerId;
		return $http.put(
			peersURL + 'reject/', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

	function approvePeerRequest(peerId) {
		var postData = 'approvePeerId=' + peerId;
		return $http.put(
			peersURL + 'approve/', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

}
