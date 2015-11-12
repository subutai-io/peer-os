'use strict';

angular.module('subutai.plugins.hadoop.service',[])
    .factory('hadoopSrv', hadoopSrv);

hadoopSrv.$inject = ['$http'];
function hadoopSrv($http) {
    var hadoopUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var hadoopSrv = {
        gethadoop: gethadoop
    };
    return hadoopSrv;
    function gethadoop() {
        return $http.get(hadoopUrl);
    }
}