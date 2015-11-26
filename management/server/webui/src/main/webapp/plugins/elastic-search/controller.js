'use strict';

angular.module('subutai.plugins.elastic-search.controller', [])
    .controller('elasticSearchCtrl', elasticSearchCtrl)

elasticSearchCtrl.$inject = ['elasticSearchSrv'];

function elasticSearchCtrl(elasticSearchSrv)
{
    var vm = this;
    //
    //elasticSearchSrv.getelasticSearch()(function (data) {
    //    vm.elasticSearch= data;
    //});
}