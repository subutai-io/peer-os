'use strict';

angular.module('subutai.plugins.shark.service',[])
    .factory('sharkSrv', sharkSrv);

sharkSrv.$inject = ['$http'];
function sharkSrv($http) {
    var sharkUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var sharkSrv = {
        getshark: getshark
    };
    return sharkSrv;
    function getshark() {
        return $http.get(sharkUrl);
    }
}