'use strict';

angular.module('subutai.plugins.mongo.controller', [])
    .controller('mongoCtrl', mongoCtrl)

mongoCtrl.$inject = ['mongoSrv'];

function mongoCtrl(mongoSrv)
{
    var vm = this;
}