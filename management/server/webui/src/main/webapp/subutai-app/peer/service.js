'use strict';

angular.module('subutai.peer.service', [])
    .factory('peerService', peerService);


peerService.$inject = ['$http'];

function peerService($http) {
    var getAllContainersURL = 'subutai-app/peer/dummy-api/containers.json';
    var getResourceHostsURL = 'subutai-app/peer/dummy-api/resourceHosts.json';

    var getContainer1URL = 'subutai-app/peer/dummy-api/container1.json';
    var getContainer2URL = 'subutai-app/peer/dummy-api/container2.json';
    var getContainer3URL = 'subutai-app/peer/dummy-api/container3.json';
    var updateContainerURL = 'subutai-app/peer/dummy-api/update.json';
    var destroyContainerURL = 'subutai-app/peer/dummy-api/';

    var peerService = {
        getAllContainers: getAllContainers,
        getContainer: getContainer,
        getResourceHosts : getResourceHosts,
        stopContainer: stopContainer,
        startContainer: startContainer,
        checkContainer: checkContainer,
        destroyContainer: destroyContainer
    };

    return peerService;

    //// Implementation

    function getAllContainers() {
        return $http.get(getAllContainersURL);
    }

    function getContainer(resourceHost) {
        if (resourceHost == "Resource Host Hadoop1") return $http.get(getContainer1URL);
        else if (resourceHost == "Resource Host Cassandra2") return $http.get(getContainer2URL);
        else if (resourceHost == "Resource Host Spark3") return $http.get(getContainer3URL);
    }

    function stopContainer(id) {
        console.log("stop");
        return $http.get(updateContainerURL+id);
    }

    function startContainer(id) {
        console.log("start");
        return $http.get(updateContainerURL+id);
    }

    function checkContainer(id) {
        console.log("check");
        return $http.get(updateContainerURL+id);
    }

    function getResourceHosts() {
        return $http.get(getResourceHostsURL );
    }

    function destroyContainer() {
        console.log("destroy");
        return $http.get(destroyContainerURL );
    }
}