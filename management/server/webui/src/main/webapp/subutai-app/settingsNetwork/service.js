"use strict";

angular.module("subutai.settings-network.service", [])
    .factory("SettingsNetworkSrv", SettingsNetworkSrv);

SettingsNetworkSrv.$inject = ["$http"];

function SettingsNetworkSrv($http) {
    var SettingsNetworkSrv = {
        getConfig: getConfig,
        updateConfig: updateConfig
    };

    function getConfig() {
        return $http.get(SERVER_URL + "rest/v1/system/network_settings", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        });
    }

    function updateConfig(config) {
        var postData = "&securePortX1=" + config.securePortX1 + "&securePortX2=" + config.securePortX2 + "&securePortX3=" + config.securePortX3 + "&publicUrl=" + config.publicUrl + "&agentPort=" + config.agentPort + "&publicSecurePort=" + config.publicSecurePort + "&keyServer=" + config.keyServer;
        return $http.post(
            SERVER_URL + "rest/v1/system/update_network_settings",
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    return SettingsNetworkSrv;
}