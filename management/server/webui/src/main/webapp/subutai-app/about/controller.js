'use strict';

angular.module('subutai.about.controller', [])
    .controller('AboutCtrl', AboutCtrl)

AboutCtrl.$inject = ['$http', 'ngDialog', 'SweetAlert', '$scope'];


function AboutCtrl ($http, ngDialog, SweetAlert, $scope) {
    var vm = this;
    vm.info = {};
    $http.get (SERVER_URL + "rest/v1/tracker/subutai/about").success (function (data) {
        vm.info = data;
        console.log (data);
    });
}