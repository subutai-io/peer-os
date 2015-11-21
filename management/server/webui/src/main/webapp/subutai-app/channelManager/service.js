/**
 * Created by talas on 6/23/15.
 */
'use strict';

angular.module('subutai.channel-manager.service', [])
    .factory('channelManagerService', channelManagerService);


channelManagerService.$inject = ['$http'];

function channelManagerService($http) {
    var getTokensURL = 'subutai-app/channelManager/dummy-api/tokens.json';

    return {
        getTokens: getTokens,
        addToken: addToken,
        removeToken: removeToken
    };

    function getTokens() {
        return $http.get(getTokensURL);
    }

    function addToken(token) {
        return $http.post('saveNewToken', token);
    }

    function removeToken(token) {
        return $http.delete('removeToken', token);
    }
}