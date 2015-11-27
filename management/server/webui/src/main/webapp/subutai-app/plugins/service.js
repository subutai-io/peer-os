'use strict';

angular.module('subutai.plugins.service',[])
	.factory('PluginsSrv', PluginsSrv);

PluginsSrv.$inject = ['$http'];

function PluginsSrv($http) {

	var PLUGINS_URL = SERVER_URL + 'js/plugins.json';

	var PluginsSrv = {
		getPlugins: getPlugins
	};

	return PluginsSrv;

	function getPlugins() {
		return $http.get(PLUGINS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}
}
