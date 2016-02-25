"use strict";

angular.module("subutai.settings-kurjun.service", [])
    .factory("SettingsKurjunSrv", SettingsKurjunSrv);

SettingsKurjunSrv.$inject = ["$http"];

function SettingsKurjunSrv($http) {
    var SettingsKurjunSrv = {
        getConfig: getConfig,
        updateConfig: updateConfig
    };

    function getConfig() {
        return $http.get(SERVER_URL + "rest/v1/system/kurjun_settings", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        });
    }

    function updateConfig(config) {
        var postData = "globalKurjunUrls=" + config.globalKurjunUrls + "&publicDiskQuota=" + config.publicDiskQuota + "&publicThreshold=" + config.publicThreshold + "&publicTimeFrame=" + config.publicTimeFrame + "&trustDiskQuota=" + config.trustDiskQuota + "&trustThreshold=" + config.trustThreshold + "&trustTimeFrame=" + config.trustTimeFrame;
        return $http.post(
            SERVER_URL + "rest/v1/system/update_kurjun_settings",
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    return SettingsKurjunSrv;
}