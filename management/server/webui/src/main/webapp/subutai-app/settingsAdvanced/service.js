"use strict";

angular.module("subutai.settings-advanced.service", [])
    .factory("SettingsAdvancedSrv", SettingsAdvancedSrv);

SettingsAdvancedSrv.$inject = ["$http"];

function SettingsAdvancedSrv($http) {
    var SettingsAdvancedSrv = {
        getConfig: getConfig,
        updateConfig: updateConfig
    };

    function getConfig() {
        return $http.get(SERVER_URL + "rest/v1/system/advanced_settings", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        });
    }

    function updateConfig(config) {
        var postData = "&securePortX1=" + config.securePortX1 + "&securePortX2=" + config.securePortX2 + "&securePortX3=" + config.securePortX3;
        return $http.post(
            SERVER_URL + "rest/v1/system/update_network_settings",
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    return SettingsAdvancedSrv;
}