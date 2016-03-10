"use strict";

angular.module("subutai.settings-advanced.controller", [])
    .controller("SettingsAdvancedCtrl", SettingsAdvancedCtrl);


SettingsAdvancedCtrl.$inject = ["$scope", "SettingsAdvancedSrv", "SweetAlert", "$sce"];
function SettingsAdvancedCtrl($scope, SettingsAdvancedSrv, SweetAlert, $sce) {
    var vm = this;
    vm.config = {};
    vm.activeTab = "karafconsole";
    vm.getConfig = getConfig;
    vm.updateConfig = updateConfig;
    vm.saveLogs = saveLogs;
    vm.renderHtml = renderHtml;

    function getConfig() {
		$('.js-karaflogs-load-screen').addClass('lololo').show();
        SettingsAdvancedSrv.getConfig().success(function (data) {
			$('.js-karaflogs-load-screen').hide();
            vm.config = data;
        }).error(function(error){
            SweetAlert.swal("ERROR!", error, "error");
			$('.js-karaflogs-load-screen').hide();
		});
    }
    getConfig();

    function updateConfig() {
        SettingsAdvancedSrv.updateConfig(vm.config).success(function (data) {
            SweetAlert.swal("Success!", "Your settings were saved.", "success");
        }).error(function (error) {
            SweetAlert.swal("ERROR!", error, "error");
        });
    }

    function saveLogs() {
        var text = vm.config.karafLogs;
        var blob = new Blob([text], {type: "text/plain;charset=utf-8"});
        saveAs(blob, "karaflogs" + moment().format('YYYY-MM-DD HH:mm:ss') + ".txt");
    }

	function renderHtml(html_code) {
		initHighlighting();
		return $sce.trustAsHtml(html_code);
	};

	function initHighlighting() {
		$('pre code').each(function(i, block) {
			hljs.highlightBlock(block);
		});
		var codeBlock = document.getElementById('js-highlight-block');
		codeBlock.scrollTop = codeBlock.scrollHeight;
	}
}
