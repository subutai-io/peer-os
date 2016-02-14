"use strict";

angular.module("subutai.settings-security.controller", [])
    .controller("SettingsSecurityCtrl", SettingsSecurityCtrl);


SettingsSecurityCtrl.$inject = ["$scope", "SettingsSecuritySrv", "SweetAlert"];
function SettingsSecurityCtrl($scope, SettingsSecuritySrv, SweetAlert) {
    var vm = this;
    vm.config = {};

    function getConfig() {
        SettingsSecuritySrv.getConfig().success(function (data) {
            console.log(data);
            vm.config = data;
        });
    }

    getConfig();


    vm.updateConfig = updateConfig;
    function updateConfig() {
        SettingsSecuritySrv.updateConfig(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }


    vm.reverse = reverse;
    function reverse(field) {
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