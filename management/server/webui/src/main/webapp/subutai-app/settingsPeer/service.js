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
        return $http.post(
            SERVER_URL + "rest/v1/system/update_peer_settings",
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );

    }


	return SettingsPeerSrv;
}