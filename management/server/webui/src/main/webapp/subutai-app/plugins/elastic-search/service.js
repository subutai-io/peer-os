'use strict';

angular.module('subutai.plugins.elastic-search.service',[])
    .factory('elasticSearchSrv', elasticSearchSrv);

elasticSearchSrv.$inject = ['$http'];
function elasticSearchSrv($http) {
    var elasticSearchUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var elasticSearchSrv = {
        getelasticSearch: getelasticSearch
    };
    return elasticSearchSrv;
    function getelasticSearch() {
        return $http.get(elasticSearchUrl);
    }
}