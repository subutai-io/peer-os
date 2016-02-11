"use strict";

angular.module ("subutai.settings-channel.controller", [])
    .controller ("SettingsChannelCtrl", SettingsChannelCtrl);


SettingsChannelCtrl.$inject = ["$scope", "SettingsChannelSrv"];
function SettingsChannelCtrl ($scope, SettingsChannelSrv) {
    var vm = this;
    vm.config = {};

    function getConfig() {
        SettingsChannelSrv.getConfig().success (function (data) {
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