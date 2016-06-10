"use strict";

angular.module("subutai.settings-updates.controller", [])
    .controller("SettingsUpdatesCtrl", SettingsUpdatesCtrl);


SettingsUpdatesCtrl.$inject = ["$scope", "SettingsUpdatesSrv", "SweetAlert"];
function SettingsUpdatesCtrl($scope, SettingsUpdatesSrv, SweetAlert) {
    var vm = this;
    vm.config = {isUpdatesAvailable: "waiting"};

    function getConfig() {
        LOADING_SCREEN();
        SettingsUpdatesSrv.getConfig().success(function (data) {
            LOADING_SCREEN('none');
            vm.config = data;
        }).error(function(error) {
            LOADING_SCREEN('none');
            SweetAlert.swal("ERROR!", error, "error");
		});
    }

    getConfig();


    vm.update = update;
    function update() {
        LOADING_SCREEN();
        SettingsUpdatesSrv.update(vm.config).success(function (data) {
            LOADING_SCREEN('none');
            localStorage.removeItem('notifications');
            SweetAlert.swal("Success!", "Subutai Successfully updated.", "success");
			getConfig();
        }).error(function (error) {
            LOADING_SCREEN('none');
            //SweetAlert.swal("ERROR!", "Save config error: " + error, "error");
			//getConfig();
        });
    }
}
