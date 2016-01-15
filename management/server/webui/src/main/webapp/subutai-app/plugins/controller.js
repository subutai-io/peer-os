'use strict';

angular.module('subutai.plugins.controller', [])
	.controller('PluginsCtrl', PluginsCtrl);

PluginsCtrl.$inject = ['PluginsSrv', 'cfpLoadingBar'];
function PluginsCtrl(PluginsSrv, cfpLoadingBar) {

	var vm = this;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.plugins = [];

	function getPlugins() {
		try {
			PluginsSrv.getPlugins().success(function(data) {
				vm.plugins = data;
			});
		} catch(e) {}
	}
	getPlugins();
}
