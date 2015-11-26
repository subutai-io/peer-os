'use strict';

angular.module('subutai.plugins.spark.service',[])
    .factory('sparkSrv', sparkSrv);

sparkSrv.$inject = ['$http'];
function sparkSrv($http) {
    var sparkUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var sparkSrv = {
        getspark: getspark
    };
    return sparkSrv;
    function getspark() {
        return $http.get(sparkUrl);
    }
}