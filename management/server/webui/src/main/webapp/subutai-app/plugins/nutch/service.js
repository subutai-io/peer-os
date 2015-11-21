'use strict';

angular.module('subutai.plugins.nutch.service',[])
    .factory('nutchSrv', nutchSrv);

nutchSrv.$inject = ['$http'];
function nutchSrv($http) {
    var nutchUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var nutchSrv = {
        getnutch: getnutch
    };
    return nutchSrv;
    function getnutch() {
        return $http.get(nutchUrl);
    }
}