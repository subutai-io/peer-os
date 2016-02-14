"use strict";

angular.module("subutai.settings-security.service", [])
    .factory("SettingsSecuritySrv", SettingsSecuritySrv);

SettingsSecuritySrv.$inject = ["$http"];

function SettingsSecuritySrv($http) {
    var SettingsSecuritySrv = {
        getConfig: getConfig,
        updateConfig: updateConfig
    };

    function getConfig() {
        return $http.get(SERVER_URL + "rest/v1/system/security_settings", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        });
    }

    function updateConfig(config) {
        console.log(config);
        var postData = "encryptionEnabled=" + config.encryptionState + "&restEncryptionEnabled=" + config.restEncryptionState + "&integrationEnabled=" + config.integrationState + "&keyTrustCheckEnabled=" + config.keyTrustCheckState;
        return $http.post(
            SERVER_URL + "rest/v1/system/update_security_settings",
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    return SettingsSecuritySrv;
}