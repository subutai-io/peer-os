'use strict';

angular.module('subutai.plugins.service',[])
	.factory('PluginsSrv', PluginsSrv);

PluginsSrv.$inject = ['$http'];

function PluginsSrv($http) {

	var pluginsUrl = 'js/plugins.json';

	var PluginsSrv = {
		getPlugins: getPlugins
	};

	return PluginsSrv;

	function getPlugins() {
		return $http.get(pluginsUrl, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}
}
