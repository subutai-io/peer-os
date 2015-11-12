'use strict';

angular.module('subutai.plugins.flume.service',[])
    .factory('flumeSrv', flumeSrv);

flumeSrv.$inject = ['$http'];
function flumeSrv($http) {
    var flumeUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var flumeSrv = {
        getflume: getflume
    };
    return flumeSrv;
    function getflume() {
        return $http.get(flumeUrl);
    }
}