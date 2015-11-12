'use strict';

angular.module('subutai.peer-registration.controller', [])
	.controller('PeerRegistrationCtrl', PeerRegistrationCtrl);

PeerRegistrationCtrl.$inject = ['$scope', 'peerRegistrationService', 'SweetAlert'];

function PeerRegistrationCtrl($scope, peerRegistrationService, SweetAlert) {

	var vm = this;
	var peersList = {};

	// functions
	vm.addPanel = addPanel;
	vm.addPeer = addPeer;
	vm.rejectPeerRequest = rejectPeerRequest;
	vm.approvePeerRequest = approvePeerRequest;

	getPeers();

	$scope.closePanel = closePanel;

	function addPeer(newPeer) {
		console.log(newPeer);
		var postData = 'ip=' + newPeer.ip + '&key_phrase=' + newPeer.keyphrase;
		peerRegistrationService.registerRequest(postData).success(function (data) {
			getPeers();
		});
	}

	function getPeers() {
		peerRegistrationService.getRequestedPeers().success(function (data) {
			vm.peersList = data;
		});
	}

	function rejectPeerRequest(peerId) {
		peerRegistrationService.rejectPeerRequest(peerId).success(function (data) {
			getPeers();
		});
	}

	function approvePeerRequest(peerId) {
		peerRegistrationService.approvePeerRequest(peerId).success(function (data) {
			getPeers();
		});
	}

	//// Implementation

	function addPanel(action) {
		jQuery('#resizable-pane').removeClass('fullWidthPane');
		if( action == 'createPeer' ) {
			jQuery('#create-peer-form').css('display', 'block');
			jQuery('#create-peer-form').removeClass('bounceOutRight');
			jQuery('#create-peer-form').addClass('animated bounceInRight');
		}
	}

	function closePanel(action) {
		jQuery('#resizable-pane').addClass('fullWidthPane');
		if( action == 'createPeer' ) {
			jQuery('#create-peer-form').addClass('bounceOutRight');
			jQuery('#create-peer-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#create-peer-form').css('display', 'none');
		}
	}

}
