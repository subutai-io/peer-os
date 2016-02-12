"use strict";

angular.module ("subutai.settings-network.service",[])
    .factory ("SettingsNetworkSrv", SettingsNetworkSrv);

SettingsNetworkSrv.$inject = ["$http"];

function SettingsNetworkSrv ($http) {
    var SettingsNetworkSrv = {
        getConfig: getConfig,
        updateConfig: updateConfig
    };

    function getConfig() {
        return $http.get (SERVER_URL + "rest/v1/system/network_settings", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
    }

    function updateConfig (config) {
        var postData = "ports=" + config.globalKurjunUrls;
        return $http.post(
            SERVER_URL + "",
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    return SettingsNetworkSrv;
}