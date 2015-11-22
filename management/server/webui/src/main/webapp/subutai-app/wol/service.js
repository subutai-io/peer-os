'use strict';

angular.module('subutai.wol.service', [])
    .factory('wolService', wolService);


wolService.$inject = ['$http'];

function wolService($http) {
    var getAllContainersURL = 'subutai-app/plugins/dummy-api/plugins.json';

    var wolService = {
        getTestComand: getTestComand
    };

    return wolService;

    // Implementation

    function getTestComand() {
        return '';
    }
}
