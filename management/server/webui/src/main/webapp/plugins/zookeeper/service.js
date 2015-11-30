'use strict';

angular.module('subutai.plugins.zookeeper.service',[])
    .factory('zookeeperSrv', zookeeperSrv);

zookeeperSrv.$inject = ['$http'];
function zookeeperSrv($http) {
    var zookeeperUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var zookeeperSrv = {
        getzookeeper: getzookeeper
    };
    return zookeeperSrv;
    function getzookeeper() {
        return $http.get(zookeeperUrl);
    }
}