'use strict';

angular.module('subutai.plugins.pig.service',[])
    .factory('pigSrv', pigSrv);

pigSrv.$inject = ['$http'];
function pigSrv($http) {
    var pigUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var pigSrv = {
        getpig: getpig
    };
    return pigSrv;
    function getpig() {
        return $http.get(pigUrl);
    }
}