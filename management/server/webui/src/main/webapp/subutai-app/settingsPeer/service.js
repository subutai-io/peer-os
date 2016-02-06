"use strict";

angular.module ("subutai.settings-peer.service",[])
	.factory ("SettingsPeerSrv", SettingsPeerSrv);

SettingsPeerSrv.$inject = ["$http"];

function SettingsPeerSrv ($http) {
	var SettingsPeerSrv = {
		getConfig: getConfig,
		updateConfig: updateConfig
	};

	function getConfig() {
		return $http.get (SERVER_URL + "rest/v1/system/peer_settings", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function updateConfig (config) {
		var postData = "externalIpInterface=" + config.externalIpInterface + "&encryptionEnabled=" + config.encryptionEnabled + "&restEncryptionEnabled=" + config.restEncryptionEnabled + "&integrationEnabled=" + config.integrationEnabled + "&keyTrustCheckEnabled=" + config.keyTrustCheckEnabled;
		return $http.post(
			SERVER_URL + "",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}


	return SettingsPeerSrv;
}