'use strict';

angular.module('subutai.plugins.oozie.service',[])
    .factory('oozieSrv', oozieSrv);

oozieSrv.$inject = ['$http'];
function oozieSrv($http) {
    var oozieUrl = 'subutai-app/plugins/dummy-api/plugins.json';
    var oozieSrv = {
        getoozie: getoozie
    };
    return oozieSrv;
    function getoozie() {
        return $http.get(oozieUrl);
    }
}