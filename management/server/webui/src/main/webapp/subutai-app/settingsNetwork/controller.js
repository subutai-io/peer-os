"use strict";

angular.module("subutai.settings-network.controller", [])
    .controller("SettingsNetworkCtrl", SettingsNetworkCtrl);


SettingsNetworkCtrl.$inject = ["$rootScope", "$scope", "SettingsNetworkSrv", "SweetAlert"];
function SettingsNetworkCtrl($rootScope, $scope, SettingsNetworkSrv, SweetAlert) {
    var vm = this;
    vm.config = {};

    function getConfig() {
        SettingsNetworkSrv.getConfig().success(function (data) {
            vm.config = data;
        });
    }

    getConfig();


    vm.updateConfig = updateConfig;
    function updateConfig() {

        if (vm.config.publicSecurePort == undefined) {
            vm.config.publicSecurePort = 8444
        }

        if (parseInt(vm.config.publicSecurePort) < 0 || parseInt(vm.config.publicSecurePort) > 65535) {
            SweetAlert.swal("ERROR!", 'Public Secure Port should be in range 0..65535', "error");
        }
        else if(!$.trim(vm.config.publicUrl)){
            SweetAlert.swal("ERROR!", 'Invalid Public URL', "error");
        }
        else {
            SettingsNetworkSrv.updateConfig(vm.config).success(function (data) {
                SweetAlert.swal("Success!", "Your settings were saved.", "success");
            }).error(function (error) {
                SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
            });
        }
    }
}