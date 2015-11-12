/**
 * Created by ubuntu on 5/14/15.
 */
'use strict';

angular.module('subutai.tracker.controller', [])
	.controller('TrackerCtrl', TrackerCtrl);


TrackerCtrl.$inject = ['trackerSrv', '$scope'];
function TrackerCtrl(trackerSrv, $scope) {

	var vm = this;
	vm.loadOperations = loadOperations;
	vm.viewLogs = viewLogs;
	vm.initDataTable = initDataTable;

	vm.startDate = new Date("2015-01-01");
	vm.endDate = new Date("2015-12-31");
	vm.logText;

	trackerSrv.getModules().success(function (data) {
		vm.modules = data;
	});

	function loadOperations() {

		var startDateString = vm.startDate.getFullYear() + '-' 
			+ vm.startDate.getMonthFormatted() + '-' 
			+ vm.startDate.getDateFormatted();
		var endDateString = vm.endDate.getFullYear() + '-' 
			+ vm.endDate.getMonthFormatted() + '-' 
			+ vm.endDate.getDateFormatted();

		trackerSrv.getOperations(vm.selectedModule, startDateString, endDateString, 10).success(function (data) {
			vm.operations = data;
			console.log(vm.operations);
		});
	}

	function viewLogs(id) {
		$('#logsContainer').html('');
		trackerSrv.getOperation(vm.selectedModule, id).success(function (data) {
			vm.logText = data.log.replace(/(?:\r\n|\r|\n)/g, '<br />');;
			$('#logsContainer').html(vm.logText);
		});		
	}

	function initDataTable() {
		$('#operationsTable').DataTable({
			responsive: true
		});
	}
}

Date.prototype.getMonthFormatted = function() {
	var month = this.getMonth() + 1;
	return month < 10 ? '0' + month : '' + month;
}

Date.prototype.getDateFormatted = function() {
	var day = this.getDate();
	return day < 10 ? '0' + day : '' + day;
}
