'use strict';

angular.module('subutai.plugins.hipi.service',[])
    .factory('hipiSrv', hipiSrv);

hipiSrv.$inject = ['$http'];
function hipiSrv($http) {
    var hipiUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var hipiSrv = {
        gethipi: gethipi
    };
    return hipiSrv;
    function gethipi() {
        return $http.get(hipiUrl);
    }
}