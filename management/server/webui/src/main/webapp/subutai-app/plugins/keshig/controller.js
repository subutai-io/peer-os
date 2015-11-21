'use strict';

angular.module('subutai.plugins.keshig.controller', [])
    .controller('KeshigCtrl', KeshigCtrl);

KeshigCtrl.$inject = ['keshigSrv', 'SweetAlert'];
function KeshigCtrl(keshigSrv, SweetAlert) {
    var vm = this;
	vm.activeTab = 'servers';
	vm.keshigInstall = {};

	//functions
	//vm.showContainers = showContainers;
	
	keshigSrv.getEnvironments().success(function (data) {
		vm.environments = data;
	});

}

