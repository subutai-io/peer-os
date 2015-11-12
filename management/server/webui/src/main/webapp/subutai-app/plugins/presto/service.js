'use strict';

angular.module('subutai.plugins.presto.service',[])
    .factory('prestoSrv', prestoSrv);

prestoSrv.$inject = ['$http'];
function prestoSrv($http) {
    var prestoUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var prestoSrv = {
        getpresto: getpresto
    };
    return prestoSrv;
    function getpresto() {
        return $http.get(prestoUrl);
    }
}