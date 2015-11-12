'use strict';

angular.module('subutai.plugins.cassandra.service',[])
    .factory('cassandraSrv', cassandraSrv);

cassandraSrv.$inject = ['$http'];
function cassandraSrv($http) {
    var cassandraUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var cassandraSrv = {
        getCassandra: getCassandra
    };
    return cassandraSrv;
    function getCassandra() {
        return $http.get(cassandraUrl);
    }
}