"use strict";

angular.module("subutai.settings-advanced.controller", [])
    .controller("SettingsAdvancedCtrl", SettingsAdvancedCtrl);


SettingsAdvancedCtrl.$inject = ["$scope", "SettingsAdvancedSrv", "SweetAlert"];
function SettingsAdvancedCtrl($scope, SettingsAdvancedSrv, SweetAlert) {
    var vm = this;
    vm.config = {};
    vm.activeTab = "karafconsole";
    vm.getConfig = getConfig;
    vm.updateConfig = updateConfig;
    vm.saveLogs = saveLogs;

    function getConfig() {
		$('.js-karaflogs-load-screen').addClass('lololo').show();
        SettingsAdvancedSrv.getConfig().success(function (data) {
			$('.js-karaflogs-load-screen').hide();
            vm.config = data;
        }).error(function(error){
            SweetAlert.swal("ERROR!", error.replace(/\\n/g, " "), "error");
			$('.js-karaflogs-load-screen').hide();
		});
    }
    //getConfig();

    function updateConfig() {
        SettingsAdvancedSrv.updateConfig(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", "Save config error: " + error.replace(/\\n/g, " "), "error");
        });
    }

    function saveLogs() {
        var text = vm.config.karafLogs;
        var blob = new Blob([text], {type: "text/plain;charset=utf-8"});
        saveAs(blob, "karaflogs" + moment().format('YYYY-MM-DD HH:mm:ss') + ".txt");
    }
}
