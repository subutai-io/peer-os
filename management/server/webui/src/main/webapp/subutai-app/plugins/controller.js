'use strict';

angular.module('subutai.plugins.controller', [])
	.controller('PluginsCtrl', PluginsCtrl);

PluginsCtrl.$inject = ['PluginsSrv'];
function PluginsCtrl(PluginsSrv) {

	var vm = this;
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
