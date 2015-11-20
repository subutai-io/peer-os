'use strict';

angular.module('subutai.plugins.service',[])
	.factory('PluginsSrv', PluginsSrv);

PluginsSrv.$inject = ['$http'];

function PluginsSrv($http) {
	var pluginsUrl = 'subutai-app/plugins/dummy-api/plugins.json';
	var PluginsSrv = {
		getPlugins: getPlugins
	};
	return PluginsSrv;
	function getPlugins() {
		return $http.get(pluginsUrl);
	}
}
