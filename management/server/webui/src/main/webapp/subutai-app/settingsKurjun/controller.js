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
    	});
    }
    getConfig();


    vm.updateConfigQuotas = updateConfigQuotas;
    function updateConfigQuotas() {
        SettingsKurjunSrv.updateConfigQuotas(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }

    vm.updateConfigUrls = updateConfigUrls;
    function updateConfigUrls() {
        SettingsKurjunSrv.updateConfigUrls(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }



    vm.addGlobalUrl = addGlobalUrl;
    function addGlobalUrl() {
        vm.config.globalKurjunUrls.push("");
    }

    vm.addLocalUrl = addLocalUrl;
    function addLocalUrl() {
        vm.config.localKurjunUrls.push("");
    }


    vm.removeGlobalUrl = removeGlobalUrl;
    function removeGlobalUrl(index) {
        if (vm.config.globalKurjunUrls.length !== 1) {
            vm.config.globalKurjunUrls.splice(index, 1);
        }
        else {
            vm.config.globalKurjunUrls[0] = "";
        }
    }

    vm.removeLocalUrl = removeLocalUrl;
    function removeLocalUrl(index) {
        if (vm.config.localKurjunUrls.length !== 1) {
            vm.config.localKurjunUrls.splice(index, 1);
        }
        else {
            vm.config.localKurjunUrls[0] = "";
        }
    }



}