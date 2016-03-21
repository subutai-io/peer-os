'use strict';

angular.module('subutai.bazaar.service',[])
	.factory('BazaarSrv', BazaarSrv);

BazaarSrv.$inject = ['$http'];

function BazaarSrv($http) {

	var BAZAAR_URL = SERVER_URL + "rest/v1/bazaar/";
	var PLUGIN_URL = SERVER_URL + "rest/v1/plugin-integrator/"
	var PLUGINS_URL = SERVER_URL + 'js/plugins.json';

	var BazaarSrv = {
		uploadPlugin: uploadPlugin,
		getInstalledPlugins: getInstalledPlugins,
		deletePlugin: deletePlugin,
		editPermissions: editPermissions,
		getPermissions: getPermissions,
		getHubPlugins: getHubPlugins,
		installHubPlugin: installHubPlugin,
		getInstalledHubPlugins: getInstalledHubPlugins,
		uninstallHubPlugin: uninstallHubPlugin,
		restoreHubPlugin: restoreHubPlugin,
		registerPeer: registerPeer,
		checkRegistration: checkRegistration,
		getRefOldPlugins: getRefOldPlugins,
		uninstallHubPluginWOButton: uninstallHubPluginWOButton
	};

	return BazaarSrv;

	function uploadPlugin (pluginName, pluginVersion, kar, permissions) {
		var fd = new FormData();
		fd.append('name', pluginName);
		fd.append('version', pluginVersion);
		fd.append('kar', kar);
		fd.append('permission', permissions);
		return $http.post(
			PLUGIN_URL + 'upload',
			fd,
			{transformRequest: angular.identity, headers: {'Content-Type': undefined}}
		);
	}

	function deletePlugin (plugin) {
		return $http.delete (PLUGIN_URL + "plugins/" + plugin);
	}

	function getInstalledPlugins() {
		return $http.get (PLUGIN_URL + "plugins/registered");
	}

	function editPermissions (postData) {
		return $http.post(
			PLUGIN_URL + "plugins/permission",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function getPermissions (pluginId) {
		return $http.get (PLUGIN_URL + "plugins/registered/" + pluginId);
	}



	function getHubPlugins() {
		return $http.get (BAZAAR_URL + "products", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}


	function installHubPlugin (plugin) {
		var kar = "";
		if (plugin.metadata[0].substring (plugin.metadata[0].length - 4, plugin.metadata[0].length) === ".kar") {
			kar = plugin.metadata[0];
		}
		else {
			kar = plugin.metadata[1];
		}
		var postData = "name=" + plugin.name + "&version=" + plugin.version + "&kar=" + kar + "&url=" + plugin.name.toLowerCase() + "&uid=" + plugin.id;
		console.log (postData);
		return $http.post(
			BAZAAR_URL + "install",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}


	function uninstallHubPlugin (plugin) {
		var kar = "";
		if (plugin.metadata[0].substring (plugin.metadata[0].length - 4, plugin.metadata[0].length) === ".kar") {
			kar = plugin.metadata[0];
		}
		else {
			kar = plugin.metadata[1];
		}
		var postData = "id=" + plugin.hubId + "&kar=" + kar + "&name=" + plugin.name.toLowerCase();
		console.log (postData);
		return $http.post(
			BAZAAR_URL + "uninstall",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function restoreHubPlugin (plugin) {
		var kar = "";
		if (plugin.metadata[0].substring (plugin.metadata[0].length - 4, plugin.metadata[0].length) === ".kar") {
			kar = plugin.metadata[0];
		}
		else {
			kar = plugin.metadata[1];
		}
		var postData = "id=" + plugin.hubId + "&name=" + plugin.name + "&version=" + plugin.version + "&kar=" + kar + "&url=" + plugin.name.toLowerCase() + "&uid=" + plugin.id;
		console.log (postData);
		return $http.post(
			BAZAAR_URL + "restore",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}


	function getInstalledHubPlugins() {
		return $http.get (BAZAAR_URL + "installed", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function registerPeer() {
		return $http.post(
    		BAZAAR_URL + "register",
    		{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
    	);
	}

	function checkRegistration() {
		return $http.get (SERVER_URL + "rest/v1/hub/registration_state", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getRefOldPlugins() {
		return $http.get(PLUGINS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function uninstallHubPluginWOButton (plugin) {
		console.log (plugin);
		var postData = "id=" + plugin.hubId + "&kar=" + plugin.name.toLowerCase() + "&name=" + plugin.name.toLowerCase();
		console.log (postData);
		return $http.post(
			BAZAAR_URL + "uninstall",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}

