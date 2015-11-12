'use strict';

angular.module('subutai.plugins.cassandra.controller', [])
    .controller('cassandraCtrl', cassandraCtrl)

cassandraCtrl.$inject = ['cassandraSrv'];
function cassandraCtrl(cassandraSrv)
{
    var vm = this;
    //cassandraSrv.getCassandra()(function (data) {
    //    vm.cassandra= data;
    //});
}