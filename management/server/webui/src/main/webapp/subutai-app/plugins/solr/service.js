'use strict';

angular.module('subutai.plugins.solr.service',[])
    .factory('solrSrv', solrSrv);

solrSrv.$inject = ['$http'];
function solrSrv($http) {
    var solrUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var solrSrv = {
        getsolr: getsolr
    };
    return solrSrv;
    function getsolr() {
        return $http.get(solrUrl);
    }
}