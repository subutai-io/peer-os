"use strict";

angular.module ("subutai.settings-peer.controller", [])
	.controller ("SettingsPeerCtrl", SettingsPeerCtrl);


SettingsPeerCtrl.$inject = ["$scope", "SettingsPeerSrv", "SweetAlert"];
function SettingsPeerCtrl ($scope, SettingsPeerSrv, SweetAlert) {
	var vm = this;
	vm.config = {};

	function getConfig() {
		SettingsPeerSrv.getConfig().success (function (data) {
			vm.config = data;
			if (vm.config.encryptionState === "true") {
				vm.config.encryptionState = true;
			}
			else {
				vm.config.encryptionState = false;
			}
			if (vm.config.restEncryptionState === "true") {
				vm.config.restEncryptionState = true;
			}
			else {
				vm.config.restEncryptionState = false;
			}
			if (vm.config.integrationState === "true") {
				vm.config.integrationState = true;
			}
			else {
				vm.config.integrationState = false;
			}
			if (vm.config.keyTrustCheckState === "true") {
				vm.config.keyTrustCheckState = true;
			}
			else {
				vm.config.keyTrustCheckState = false;
			}
		});
	}
	getConfig();


	vm.updateConfig = updateConfig;
	function updateConfig() {
		SettingsPeerSrv.updateConfig (vm.config).success (function (data) {
			SweetAlert.swal ("Success!", "Your settings were saved.", "success");
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
		});
	}

	vm.reverse = reverse;
	function reverse (field) {
		switch (field) {
			case (0):
				vm.config.encryptionEnabled = !vm.config.encryptionEnabled;
				break;
			case (1):
				vm.config.restEncryptionEnabled = !vm.config.restEncryptionEnabled;
				break;
			case (2):
				vm.config.integrationEnabled = !vm.config.integrationEnabled;
				break;
			case (3):
				vm.config.keyTrustCheckEnabled = !vm.config.keyTrustCheckEnabled;
				break;
		}
	}
}
