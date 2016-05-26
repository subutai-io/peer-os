"use strict";

angular.module("subutai.settings-updates.service", [])
    .factory("SettingsUpdatesSrv", SettingsUpdatesSrv);

SettingsUpdatesSrv.$inject = ["$http"];

function SettingsUpdatesSrv($http) {
    var SettingsUpdatesSrv = {
        getConfig: getConfig,
        update: update
    };

    function getConfig() {
        return $http.get(SERVER_URL + "rest/v1/system/management_updates", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        });
    }

    function update() {
        return $http.post(SERVER_URL + "rest/v1/system/update_management", {
            withCredentials: true,
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        });
    }


    return SettingsUpdatesSrv;
}