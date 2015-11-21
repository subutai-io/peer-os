'use strict';

angular.module('subutai.plugins.mahout.service',[])
    .factory('mahoutSrv', mahoutSrv);

mahoutSrv.$inject = ['$http'];
function mahoutSrv($http) {
    var mahoutUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var mahoutSrv = {
        getmahout: getmahout
    };
    return mahoutSrv;
    function getmahout() {
        return $http.get(mahoutUrl);
    }
}