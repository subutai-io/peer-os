'use strict';

angular.module('subutai.about.controller', [])
	.controller('AboutCtrl', AboutCtrl)

AboutCtrl.$inject = ['$http', 'ngDialog', 'SweetAlert', '$scope', '$timeout'];


function AboutCtrl ($http, ngDialog, SweetAlert, $scope, $timeout) {
	var vm = this;
	vm.info = {};
	vm.policyConfig = {};
	vm.currentType = 'about';
	vm.statusTable = {
		"p2pStatuses": {
			"healthy": 0,
			"problems": 0,
			"notWork": 0,
		},
		"p2pUpdates": {
			"updated": 0,
			"normal": 0,
			"needUpdate": 0,
		}
	};

	vm.statusColors = [
		{"color": "#22b573", "text": "Already up-to-date", "status": "true", "statusText": "Healthy"},
		{"color": "#efc94c", "text": "Need update soon", "status": "WAIT", "statusText": "Problems"},
		{"color": "#c1272d", "text": "Update immediately", "status": "false", "statusText": "Not working"},
	];

	$http.get (SERVER_URL + "rest/v1/system/about").success (function (data) {
		vm.info = data;
		for (var rhId in vm.info.peerP2PVersions) {
			switch (vm.info.peerP2PVersions[rhId].p2pStatus) {
				case 0:
					vm.statusTable.p2pStatuses.healthy++;
					break;
				case 1:
					vm.statusTable.p2pStatuses.problems++;
					break;
				case 2:
					vm.statusTable.p2pStatuses.notWork++;
					break;
				default:
					break;
			}
			switch (vm.info.peerP2PVersions[rhId].p2pVersionCheck) {
				case 0:
					vm.statusTable.p2pUpdates.updated++;
					break;
				case 1:
					vm.statusTable.p2pUpdates.normal++;
					break;
				case 2:
					vm.statusTable.p2pUpdates.needUpdate++;
					break;
				default:
					break;
			}
		}
	});

	$http.get (SERVER_URL + "rest/v1/system/peer_policy").success (function (data) {
		vm.policyConfig = data;
	});

	vm.hasPGPplugin = false;
	$timeout(function() {
		vm.hasPGPplugin = hasPGPplugin();
	}, 2000);
}
