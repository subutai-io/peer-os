"use strict";

angular.module("subutai.settings-updates.controller", [])
    .controller("SettingsUpdatesCtrl", SettingsUpdatesCtrl);


SettingsUpdatesCtrl.$inject = ["$scope", "SettingsUpdatesSrv", "SweetAlert"];
function SettingsUpdatesCtrl($scope, SettingsUpdatesSrv, SweetAlert) {
    var vm = this;
    vm.config = {};

    function getConfig() {
        SettingsUpdatesSrv.getConfig().success(function (data) {
            LOADING_SCREEN('none');
            vm.config = data;
        });
    }

    getConfig();


    vm.update = update;
    function update() {
        SettingsUpdatesSrv.update(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }
}