'use strict';

angular.module('subutai.object-relation.service', [])
    .factory('objectRelationService', objectRelationService);


objectRelationService.$inject = ['$http'];

function objectRelationService($http) {
    var PEERS_URL = SERVER_URL + 'rest/v1/relation';
    var RH_URL = PEERS_URL + 'resource_hosts/';

    var objectRelationService = {
        issueChallenge: issueChallenge
    };

    return objectRelationService;

    //// Implementation

    function issueChallenge() {
        return $http.get(PEERS_URL + "/challenge", {withCredentials: true, headers: {'Content-Type': 'application/json'}});
    }
}
