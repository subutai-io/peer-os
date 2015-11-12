'use strict';

angular.module('subutai.plugins.controller', [])
        .controller('PluginsCtrl', PluginsCtrl);

PluginsCtrl.$inject = ['PluginsSrv'];
function PluginsCtrl(PluginsSrv) {
    var vm = this;

    PluginsSrv.getPlugins().success(function(data) {
        vm.plugins = data;
    });
}
