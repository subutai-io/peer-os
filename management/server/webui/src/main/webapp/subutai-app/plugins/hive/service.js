'use strict';

angular.module('subutai.plugins.hive.service',[])
    .factory('hiveSrv', hiveSrv);

hiveSrv.$inject = ['$http'];
function hiveSrv($http) {
    var hiveUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var hiveSrv = {
        gethive: gethive
    };
    return hiveSrv;
    function gethive() {
        return $http.get(hiveUrl);
    }
}