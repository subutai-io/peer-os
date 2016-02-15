"use strict";

angular.module ("subutai.settings-peer.service",[])
	.factory ("SettingsPeerSrv", SettingsPeerSrv);

SettingsPeerSrv.$inject = ["$http"];

function SettingsPeerSrv ($http) {
	var SettingsPeerSrv = {
		getSettingsConfig: getSettingsConfig,
		getPolicyConfig: getPolicyConfig,
		updateSettingsConfig: updateSettingsConfig,
		updatePolicyConfig: updatePolicyConfig
	};

	function getSettingsConfig() {
		return $http.get (SERVER_URL + "rest/v1/system/peer_settings", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getPolicyConfig() {
    		return $http.get (SERVER_URL + "rest/v1/system/peer_policy", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
    }

	function updateSettingsConfig (config) {
        return $http.post(
            SERVER_URL + "rest/v1/system/update_peer_settings",
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );

    }

	function updatePolicyConfig (config) {
		var postData = "peerId=" + config.peerId + "&diskUsageLimit=" + config.diskUsageLimit + "&cpuUsageLimit=" + config.cpuUsageLimit + "&memoryUsageLimit=" + config.memoryUsageLimit /*+ "&networkUsageLimit=" + config.networkUsageLimit*/ + "&environmentLimit=" + config.environmentLimit + "&containerLimit=" + config.containerLimit;
		return $http.post(
			SERVER_URL + "rest/v1/system/update_peer_policy",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	return SettingsPeerSrv;
}