"use strict";

angular.module ("subutai.settings-channel.service",[])
    .factory ("SettingsChannelSrv", SettingsChannelSrv);

SettingsChannelSrv.$inject = ["$http"];

function SettingsChannelSrv ($http) {
    var SettingsChannelSrv = {
        getConfig: getConfig,
        updateConfig: updateConfig
    };

    function getConfig() {
        return $http.get (SERVER_URL + "rest/v1/system/channel_settings", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
    }

    function updateConfig (config) {
        var postData = "ports=" + config.globalKurjunUrls;
        return $http.post(
            SERVER_URL + "",
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    return SettingsChannelSrv;
}