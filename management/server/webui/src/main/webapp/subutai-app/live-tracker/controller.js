'use strict';

LiveTrackerCtrl.$inject = ['liveTrackerSrv', '$scope', '$rootScope', '$timeout', '$sce'];


function LiveTrackerCtrl(liveTrackerSrv, $scope, $rootScope, $timeout, $sce) {

	var vm = this;
	vm.modules = [];

	vm.selectedModule = 'ENVIRONMENT MANAGER';
	vm.startDate = moment().format('YYYY-MM-DD');
	vm.endDate = moment().format('YYYY-MM-DD');
	vm.limit = 10;
	vm.currentOperattion = false;
	vm.logText = '';

	vm.getOperarions = getOperarions;
	vm.viewLogs = viewLogs;
	vm.setHtml = setHtml;
	vm.closeLog = closeLog;

	liveTrackerSrv.getModules().success(function (data) {
		vm.modules = data;
	});

	function loadOperations() {
		liveTrackerSrv.getOperations(vm.selectedModule, vm.startDate, vm.endDate, vm.limit).success(function (data) {
			vm.operations = data;
		});
	}

	var refreshCurrentOperations;
	var reloadCurrentOperations = function() {
		refreshCurrentOperations = $timeout(function myFunction() {
			if(vm.selectedModule) {
				loadOperations();
			}
			refreshCurrentOperations = $timeout(reloadCurrentOperations, 3000);
		}, 3000);
	};
	reloadCurrentOperations();

	function getOperarions(module) {
		if(module === null) return;
		if(module != vm.selectedModule) {
			vm.selectedModule = module;
			vm.currentOperattion = false;
			loadOperations();
		}
	}
	loadOperations();

	function viewLogs(id) {
		vm.currentOperattion = id;
		vm.logText = '';
		getLogs(id);
	}

	function getLogs(id) {
		liveTrackerSrv.getOperation(vm.selectedModule, vm.currentOperattion).success(function (data) {
			vm.logText = data.log.replace(/(?:\r\n|\r|\n)/g, '<br />');
		});
	}	

	var refreshCurrentLog;
	var reloadCurrentLog = function() {
		refreshCurrentLog = $timeout(function myFunction() {
			if(vm.currentOperattion) {
				getLogs(vm.currentOperattion);
			}
			refreshCurrentLog = $timeout(reloadCurrentLog, 1000);
		}, 1000);
	};
	reloadCurrentLog();

	function setHtml(html) {
		return $sce.trustAsHtml(html.toString());
	};

	function closeLog() {
		vm.currentOperattion = false;
		vm.logText = '';
	}

}

