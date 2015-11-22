'use strict';

angular.module('subutai.core.service',[])
    .factory('coreSrv', coreSrv);

coreSrv.$inject = ['$http'];
function coreSrv($http) {
    var coreUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var coreSrv = {
        getCore: getCore
    };
    return coreSrv;
    function getCore() {
        return $http.get(coreUrl);
    }
}