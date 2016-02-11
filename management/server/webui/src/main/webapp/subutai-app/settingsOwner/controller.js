"use strict";

angular.module ("subutai.settings-owner.controller", [])
    .controller ("SettingsOwnerCtrl", SettingsOwnerCtrl);


SettingsOwnerCtrl.$inject = ["$scope", "SettingsOwnerSrv", "SweetAlert"];
function SettingsOwnerCtrl ($scope, SettingsOwnerSrv, SweetAlert) {
    var vm = this;
    vm.config = {};

    function getConfig() {
        SettingsOwnerSrv.getConfig().success (function (data) {
            console.log(data);
            vm.config = data;
        });
    }
    getConfig();


    vm.updateConfig = updateConfig;
    function updateConfig() {
        SettingsOwnerSrv.updateConfig (vm.config).success (function (data) {
            SweetAlert.swal ("Success!", "Your settings were saved.", "success");
        }).error (function (error) {
            SweetAlert.swal ("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }
}