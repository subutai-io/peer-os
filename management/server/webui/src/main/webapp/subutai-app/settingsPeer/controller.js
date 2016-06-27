"use strict";

angular.module ("subutai.settings-peer.controller", [])
	.controller ("SettingsPeerCtrl", SettingsPeerCtrl);


SettingsPeerCtrl.$inject = ["$scope", "SettingsPeerSrv", "SweetAlert"];
function SettingsPeerCtrl ($scope, SettingsPeerSrv, SweetAlert) {
	var vm = this;
	vm.settingsConfig = {};
	vm.activeTab = "settings";
	function getConfig() {
		SettingsPeerSrv.getSettingsConfig().success (function (data) {
			vm.settingsConfig = data;
		});
		/*SettingsPeerSrv.getPolicyConfig().success (function (data) {
			vm.policyConfig = data;
		});*/
	}
	getConfig();


	vm.updateSettingsConfig = updateSettingsConfig;
	function updateSettingsConfig() {
		SettingsPeerSrv.updateSettingsConfig (vm.settingsConfig).success (function (data) {
			SweetAlert.swal ("Success!", "Your settings were saved.", "success");
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
		});
	}


	vm.updatePolicyConfig = updatePolicyConfig;
	function updatePolicyConfig() {
		SettingsPeerSrv.updatePolicyConfig (vm.policyConfig).success (function (data) {
			SweetAlert.swal ("Success!", "Your settings were saved.", "success");
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
		});
	}
}
