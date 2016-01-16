'use strict';

angular.module('subutai.plugin_integrator.service',[])
	.factory('IntegratorSrv', IntegratorSrv);

IntegratorSrv.$inject = ['$http'];

function IntegratorSrv($http) {

	var BASE_URL = SERVER_URL + "rest/v1/plugininjector/";

	var IntegratorSrv = {
		uploadPlugin: uploadPlugin,
		getInstalledPlugins: getInstalledPlugins,
		deletePlugin: deletePlugin,
		editPermissions: editPermissions,
		getPermissions: getPermissions
	};

	return IntegratorSrv;

	function uploadPlugin (pluginName, pluginVersion, kar, permissions) {
		console.log (pluginName);
		console.log (pluginVersion);
		console.log (kar);
		console.log (permissions);
		var fd = new FormData();
		fd.append('name', pluginName);
		fd.append('version', pluginVersion);
		fd.append('kar', kar);
		fd.append('permission', permissions);
		return $http.post(
			BASE_URL + 'upload',
			fd,
			{transformRequest: angular.identity, headers: {'Content-Type': undefined}}
		);
	}

	function deletePlugin (plugin) {
		return $http.delete (BASE_URL + "plugins/" + plugin);
	}

	function getInstalledPlugins() {
		return $http.get (BASE_URL + "plugins/registered");
	}

	function editPermissions (postData) {
		return $http.post(
			BASE_URL + "plugins/permission",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function getPermissions (pluginId) {
		return $http.get (BASE_URL + "plugins/registered/" + pluginId);
	}
}
