"use strict";

angular.module("subutai.settings-kurjun.service", [])
    .factory("SettingsKurjunSrv", SettingsKurjunSrv);

SettingsKurjunSrv.$inject = ["$http"];

function SettingsKurjunSrv($http) {
    var BASE_URL = SERVER_URL + 'rest/v1/kurjun-manager/';
    var URLS_LIST_URL = BASE_URL + 'urls';
    var REGISTER_URL = BASE_URL + 'register';
    var SIGNED_MESSAGE_URL = BASE_URL + 'signed-msg';
    var ADD_URL = BASE_URL + 'url/add';

    var SettingsKurjunSrv = {
        getConfig: getConfig,
        addUrl: addUrl,
        registerUrl: registerUrl,
        signedMsg: signedMsg,
        updateConfigUrls: updateConfigUrls,
        updateConfigQuotas: updateConfigQuotas,
        getUrlsListUrl: function () {
            return URLS_LIST_URL
        }
    };

    function getConfig() {
        return $http.get(SERVER_URL + "rest/v1/system/kurjun_settings", {
            withCredentials: true,
            headers: {'Content-Type': 'application/json'}
        });
    }

    function addUrl(postData) {
        return $http.post(
            ADD_URL,
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    function registerUrl(id) {
        var postData = "id=" + id;
        return $http.post(
            REGISTER_URL,
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }

    function signedMsg(id, signedMsg) {
        var postData = "signedMsg=" + signedMsg + "&id=" + id;
        return $http.post(
            SIGNED_MESSAGE_URL,
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }

    function updateConfigUrls(config) {
        var postData = "globalKurjunUrls=" + config.globalKurjunUrls + "&localKurjunUrls=" + config.localKurjunUrls;
        return $http.post(
            SERVER_URL + "rest/v1/system/update_kurjun_settings_urls",
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }

    function updateConfigQuotas(config) {
        var postData = "&publicDiskQuota=" + config.publicDiskQuota + "&publicThreshold=" + config.publicThreshold + "&publicTimeFrame=" + config.publicTimeFrame + "&trustDiskQuota=" + config.trustDiskQuota + "&trustThreshold=" + config.trustThreshold + "&trustTimeFrame=" + config.trustTimeFrame;
        return $http.post(
            SERVER_URL + "rest/v1/system/update_kurjun_settings_quotas",
            postData,
            {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
        );
    }


    return SettingsKurjunSrv;
}
