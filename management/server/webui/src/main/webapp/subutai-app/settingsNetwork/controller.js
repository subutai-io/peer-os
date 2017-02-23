"use strict";

angular.module("subutai.settings-network.controller", [])
    .controller("SettingsNetworkCtrl", SettingsNetworkCtrl);


SettingsNetworkCtrl.$inject = ["$rootScope", "$scope", "SettingsNetworkSrv", "SweetAlert"];
function SettingsNetworkCtrl($rootScope, $scope, SettingsNetworkSrv, SweetAlert) {
    var vm = this;
    vm.config = {};

    function getConfig() {
        SettingsNetworkSrv.getConfig().success(function (data) {
            console.log(data);
            vm.config = data;
        });
    }

    getConfig();


    vm.updateConfig = updateConfig;
    function updateConfig() {
        if (vm.config.startRange == undefined) {
            vm.config.startRange = 0
        }

        if (vm.config.endRange == undefined) {
            vm.config.endRange = 65535
        }

        if (vm.config.publicSecurePort == undefined) {
            vm.config.publicSecurePort = 8444
        }

        if (parseInt(vm.config.publicSecurePort) < 0 || parseInt(vm.config.publicSecurePort) > 65535) {
            SweetAlert.swal("ERROR!", 'Public Secure Port should be 0..65535', "error");
        }
        else if (parseInt(vm.config.startRange) < 0 || parseInt(vm.config.startRange) > 65535) {
            SweetAlert.swal("ERROR!", 'P2P port start range should be 0..65535', "error");
        }
        else if (parseInt(vm.config.endRange) < 0 || parseInt(vm.config.endRange) > 65535) {
            SweetAlert.swal("ERROR!", 'P2P port end range should be 0..65535', "error");
        }
        else if (parseInt(vm.config.startRange) > parseInt(vm.config.endRange)) {
            SweetAlert.swal("ERROR!", 'P2P port start range can not be bigger than end range', "error");
        }
        else if(!$.trim(vm.config.publicUrl)){
            SweetAlert.swal("ERROR!", 'Invalid Public URL', "error");
        }
        else if(!$.trim(vm.config.hubIp)){
            SweetAlert.swal("ERROR!", 'Invalid Hub IP', "error");
        }
        else {
            SettingsNetworkSrv.updateConfig(vm.config).success(function (data) {
                $rootScope.$broadcast('hubIpSet', {});
                SweetAlert.swal("Success!", "Your settings were saved.", "success");
            }).error(function (error) {
                SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
            });
        }
    }
}