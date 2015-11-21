'use strict';

angular.module('subutai.peer-registration.service', [])
	.factory('peerRegistrationService', peerRegistrationService);


peerRegistrationService.$inject = ['$http'];

function peerRegistrationService($http) {
	var peersURL = serverUrl + 'peer_ui/';

	var peerRegistrationService = {
		getRequestedPeers: getRequestedPeers,
		registerRequest: registerRequest,
		rejectPeerRequest: rejectPeerRequest,
		approvePeerRequest: approvePeerRequest,
		cancelPeerRequest: cancelPeerRequest,
		unregisterPeerRequest: unregisterPeerRequest
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
		var postData = 'peerId=' + peerId;
		return $http.put(
			peersURL + 'reject/', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

	function unregisterPeerRequest(peerId) {
		var postData = 'peerId=' + peerId;
		return $http.put(
			peersURL + 'unregister/', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

	function cancelPeerRequest(peerId) {
		var postData = 'peerId=' + peerId;
		return $http.put(
			peersURL + 'cancel/', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

	function approvePeerRequest(peerId, keyPhrase) {
		var postData = 'peerId=' + peerId + '&key_phrase=' + keyPhrase;
		return $http.put(
			peersURL + 'approve/', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);		
	}

}
