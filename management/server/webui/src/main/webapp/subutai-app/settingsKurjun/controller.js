"use strict";

angular.module("subutai.settings-kurjun.controller", [])
    .controller("SettingsKurjunCtrl", SettingsKurjunCtrl);


SettingsKurjunCtrl.$inject = ["$scope", "SettingsKurjunSrv", "SweetAlert"];
function SettingsKurjunCtrl($scope, SettingsKurjunSrv, SweetAlert) {
    var vm = this;
    vm.config = {globalKurjunUrls: [""]};
	vm.activeTab = "urls";
    function getConfig() {
        SettingsKurjunSrv.getConfig().success(function (data) {
            vm.config = data;
    }

    getConfig();


    vm.updateConfig = updateConfig;
    function updateConfig() {
        SettingsKurjunSrv.updateConfig(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }


    vm.addUrl = addUrl;
    function addUrl() {
        vm.config.globalKurjunUrls.push("");
    }

    vm.removeUrl = removeUrl;
    function removeUrl(index) {
        if (vm.config.globalKurjunUrls.length !== 1) {
            vm.config.globalKurjunUrls.splice(index, 1);
        }
        else {
            vm.config.globalKurjunUrls[0] = "";
        }
    }


}