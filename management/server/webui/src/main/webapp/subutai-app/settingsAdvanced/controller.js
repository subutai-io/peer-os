"use strict";

angular.module ("subutai.settings-advanced.controller", [])
    .controller ("SettingsAdvancedCtrl", SettingsAdvancedCtrl);


SettingsAdvancedCtrl.$inject = ["$scope", "SettingsAdvancedSrv","SweetAlert"];
function SettingsAdvancedCtrl ($scope, SettingsAdvancedSrv, SweetAlert) {
    var vm = this;
    vm.config = {};

    function getConfig() {
        SettingsAdvancedSrv.getConfig().success (function (data) {
            console.log(data);
            vm.config = data;
        });
    }
    getConfig();


    vm.updateConfig = updateConfig;
    function updateConfig() {
        SettingsAdvancedSrv.updateConfig (vm.config).success (function (data) {
            SweetAlert.swal ("Success!", "Your settings were saved.", "success");
        }).error (function (error) {
            SweetAlert.swal ("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }
}