"use strict";

angular.module("subutai.settings-updates.controller", [])
.controller("SettingsUpdatesCtrl", SettingsUpdatesCtrl);


SettingsUpdatesCtrl.$inject = ["$scope", "SettingsUpdatesSrv", "SweetAlert"];
function SettingsUpdatesCtrl($scope, SettingsUpdatesSrv, SweetAlert) {
	var vm = this;
	vm.config = {isUpdatesAvailable: "waiting"};
	vm.updateText = 'Your system is already up-to-date';

	function getConfig() {
		LOADING_SCREEN();
		SettingsUpdatesSrv.getConfig().success(function (data) {
			LOADING_SCREEN('none');
			vm.config = data;
			if(vm.config.isUpdatesAvailable == true) {
				vm.updateText = 'Update is available';
			} else {
				vm.updateText = 'Your system is already up-to-date';
			}
		}).error(function(error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", error, "error");
		});
	}

	getConfig();


	vm.update = update;
	function update() {
		LOADING_SCREEN();
		vm.updateText = 'Please wait, update is in progress. System will restart automatically';
		SettingsUpdatesSrv.update(vm.config).success(function (data) {
			LOADING_SCREEN('none');
			localStorage.removeItem('notifications');
			SweetAlert.swal("Success!", "Subutai Successfully updated.", "success");
			getConfig();
		}).error(function (error) {
			//LOADING_SCREEN('none');
			//SweetAlert.swal("ERROR!", "Save config error: " + error, "error");
			//getConfig();
			setInterval(function() {
				getConfig();
			}, 120000);
		});
	}
}
