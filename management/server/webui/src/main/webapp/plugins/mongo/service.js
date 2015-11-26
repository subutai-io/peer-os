'use strict';

angular.module('subutai.plugins.mongo.service',[])
    .factory('mongoSrv', mongoSrv);

mongoSrv.$inject = ['$http'];
function mongoSrv($http) {
    var mongoUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var mongoSrv = {
        getmongo: getmongo
    };
    return mongoSrv;
    function getmongo() {
        return $http.get(mongoUrl);
    }
}