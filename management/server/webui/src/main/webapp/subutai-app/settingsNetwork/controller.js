"use strict";

angular.module ("subutai.settings-network.controller", [])
    .controller ("SettingsNetworkCtrl", SettingsNetworkCtrl);


SettingsNetworkCtrl.$inject = ["$scope", "SettingsNetworkSrv"];
function SettingsNetworkCtrl ($scope, SettingsNetworkSrv) {
    var vm = this;
    vm.config = {};

    function getConfig() {
        SettingsNetworkSrv.getConfig().success (function (data) {
            console.log(data);
            vm.config = data;
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
}