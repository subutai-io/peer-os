"use strict";

angular.module("subutai.settings-network.controller", [])
    .controller("SettingsNetworkCtrl", SettingsNetworkCtrl);


SettingsNetworkCtrl.$inject = ["$scope", "SettingsNetworkSrv", "SweetAlert"];
function SettingsNetworkCtrl($scope, SettingsNetworkSrv, SweetAlert) {
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

        if (parseInt(vm.config.startRange) < 0 || parseInt(vm.config.startRange) > 65535) {
            SweetAlert.swal("ERROR!", 'start range should be 0..65535', "error");
        }
        else if (parseInt(vm.config.endRange) < 0 || parseInt(vm.config.endRange) > 65535) {
            SweetAlert.swal("ERROR!", 'end range should be 0..65535', "error");
        }
        else if (parseInt(vm.config.startRange) > parseInt(vm.config.endRange)) {
            SweetAlert.swal("ERROR!", 'Start range can not be bigger than End range', "error");
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