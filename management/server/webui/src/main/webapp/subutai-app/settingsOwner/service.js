"use strict";

angular.module("subutai.settings-owner.service", [])
    .factory("SettingsOwnerSrv", SettingsOwnerSrv);

SettingsOwnerSrv.$inject = ["$http"];

function SettingsOwnerSrv($http) {
    var SettingsOwnerSrv = {
        getConfig: getConfig,
        updateConfig: updateConfig
    };

    function getConfig() {
        return $http.get(SERVER_URL + "rest/v1/system/peerowner", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        });
    }

    function updateConfig(config) {
        return $http.post(
            SERVER_URL + "rest/v1/system/update_peerowner",
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    return SettingsOwnerSrv;
}