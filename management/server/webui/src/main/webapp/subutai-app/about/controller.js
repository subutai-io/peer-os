'use strict';

angular.module('subutai.about.controller', [])
	.controller('AboutCtrl', AboutCtrl)

AboutCtrl.$inject = ['$http', 'ngDialog', 'SweetAlert', '$scope', '$timeout'];


function AboutCtrl ($http, ngDialog, SweetAlert, $scope, $timeout) {
	var vm = this;
	vm.info = {};
	$http.get (SERVER_URL + "rest/v1/system/about").success (function (data) {
		vm.info = data;
		console.log (data);
	});

	vm.hasPGPplugin = false;
	$timeout(function() {
		vm.hasPGPplugin = hasPGPplugin();
		console.log(vm.hasPGPplugin);
	}, 2000);
}
