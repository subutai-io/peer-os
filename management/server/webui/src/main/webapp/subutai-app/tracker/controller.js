'use strict';

angular.module('subutai.tracker.controller', [])
	.controller('TrackerCtrl', TrackerCtrl);


TrackerCtrl.$inject = ['trackerSrv', '$scope', '$rootScope', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'ngDialog', '$timeout', 'cfpLoadingBar'];


function TrackerCtrl(trackerSrv, $scope, $rootScope, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, ngDialog, $timeout, cfpLoadingBar) {

	var vm = this;
	vm.logs = [];
	vm.currentLog = [];

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.loadOperations = loadOperations;
	vm.viewLogs = viewLogs;

	vm.selectedModule = 'ENVIRONMENT MANAGER';
	vm.endDate = new Date();
	vm.startDate = new Date();
	vm.startDate.setDate(vm.endDate.getDate()-7);

	trackerSrv.getModules().success(function (data) {
		vm.modules = data;
	});

	vm.dtInstance = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			var logsDates = getDateInStringFormat();
			var url  = trackerSrv.getBaseUrl() + 'operations/' + vm.selectedModule + '/' + logsDates.startDateString + '/' + logsDates.endDateString + '/' + 100;
			return $resource( url ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('createdRow', createdRow)
		.withOption('order', [[ 0, "desc" ]])
		.withOption('columnDefs', [ {className: "b-main-table__status-col", "targets": [2, 3]} ])
		.withOption('stateSave', true);

	vm.dtColumns = [
		DTColumnBuilder.newColumn('createDate').withTitle('Date').renderWith(dateHTML),
		DTColumnBuilder.newColumn('description').withTitle('Operation'),
		DTColumnBuilder.newColumn(null).withTitle('Status').renderWith(statusHTML),
		DTColumnBuilder.newColumn(null).withTitle('Logs').notSortable().renderWith(viewLogsButton),
	];

	var refreshTable;
	var reloadTableData = function() {
		refreshTable = $timeout(function myFunction() {
			if(typeof(vm.dtInstance.reloadData) == 'function') {
				vm.dtInstance.reloadData(null, false);
			}
			refreshTable = $timeout(reloadTableData, 30000);
		}, 30000);
	};
	reloadTableData();

	$rootScope.$on('$stateChangeStart',	function(event, toState, toParams, fromState, fromParams){
		console.log('cancel');
		$timeout.cancel(refreshTable);
	});

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function statusHTML(data, type, full, meta) {
		vm.logs[data.id] = data;
		return '<div class="b-status-icon b-status-icon_' + data.state + '" tooltips tooltip-template="' + data.state + '" tooltip-smart="true"></div>';
	}

	function dateHTML(data, type, full, meta) {
		return '<div>' + moment( data ).format('YYYY-MM-DD HH:mm:ss') + '</div>';
	}

	function viewLogsButton(data, type, full, meta) {
		return '<a href class="b-btn b-btn_green" ng-click="trackerCtrl.viewLogs(\'' + data.id + '\')">View logs</a>';
	}

	function loadOperations() {
		var logsDates = getDateInStringFormat();
		if(logsDates) {
			vm.dtInstance.reloadData(null, false);
		}
	}

	function viewLogs(id) {
		vm.currentLog = [];
		ngDialog.open({
			template: 'subutai-app/tracker/partials/logsPopup.html',
			scope: $scope
		});

		getLogById(id);
	}

	function checkLastLog(status, log) {
		if(log === undefined || log === null) log = false;
		var lastLog = vm.currentLog[vm.currentLog.length - 1];

		if(log) {
			var logObj = JSON.parse(log.substring(0, log.length - 1));
			lastLog.time = moment(logObj.date).format('HH:mm:ss');
		} else {
			lastLog.time = moment().format('HH:mm:ss');
		}

		if(status === true) {
			lastLog.status = 'success';
			lastLog.classes = ['fa-check', 'g-text-green'];
		} else {
			lastLog.status = 'success';
			lastLog.classes = ['fa-times', 'g-text-red'];
		}
	}

	function getLogById(id, checkLast, prevLogs) {
		if(checkLast === undefined || checkLast === null) checkLast = false;
		if(prevLogs === undefined || prevLogs === null) prevLogs = false;
		trackerSrv.getOperation(vm.selectedModule, id)
			.success(function (data) {
				if(data.state == 'RUNNING') {

					if(checkLast) {
						checkLastLog(true);
					}

					//var logs = data.log.split(/(?:\r\n|\r|\n)/g);
					var logs = data.log.split('},');
					var result = [];
					var i = 0;
					if(prevLogs) {
						i = prevLogs.length;
						if(logs.length > prevLogs.length) {
							checkLastLog(true);
						}
					}
					for(i; i < logs.length; i++) {

						var logCheck = logs[i].replace(/ /g,'');
						if(logCheck.length > 0) {

							//var logObj = JSON.parse(logs[i].substring(0, logs[i].length - 1));

							logs[i] = logs[i].replace(/(?:\r\n|\r|\n)/g , '');
							var logObj = JSON.parse(logs[i] + '}');
							var logTime = moment(logObj.date).format('HH:mm:ss');

							var logStatus = 'success';
							var logClasses = ['fa-check', 'g-text-green'];

							if(i+1 == logs.length) {
								logTime = '';
								logStatus = 'in-progress';
								logClasses = ['fa-spinner', 'fa-pulse'];
							}

							var  currentLog = {
								"time": logTime,
								"status": logStatus,
								"classes": logClasses,
								"log": logObj.log
							};
							result.push(currentLog);

						}
					}

					vm.currentLog = vm.currentLog.concat(result);

					setTimeout(function() {
						getLogById(id, false, logs);
					}, 2000);

					return result;
				} else {
					if(!prevLogs) {
						var logsArray = data.log.split('},');
						var logs = [];
						for(var i = 0; i < logsArray.length; i++) {
							if(logsArray[i].length > 0) {
								logsArray[i] = logsArray[i].replace(/(?:\r\n|\r|\n)/g , '');
								var currentLog = JSON.parse(logsArray[i] + '}');
								currentLog.time = moment(currentLog.date).format('HH:mm:ss');

								currentLog.classes = ['fa-check', 'g-text-green'];
								if(currentLog.state == 'FAILED') {
									currentLog.classes = ['fa-times', 'g-text-red'];
								}

								logs.push(currentLog);
							}
						}
						vm.currentLog = logs;
					} else {
						if(data.state == 'FAILED') {
							checkLastLog(false);
						} else {

							if(prevLogs) {
								var logs = data.log.split(/(?:\r\n|\r|\n)/g);
								if(logs.length > prevLogs.length) {
									checkLastLog(true, logs[logs.length-1]);
								}
							} else {
								checkLastLog(true);
							}
							var currentLog = {
								"time": moment().format('HH:mm:ss'),
								"status": 'success',
								"classes": ['fa-check', 'g-text-green'],
								"log": 'Operation has been performed successfully'
							};
							vm.currentLog.push(currentLog);						
						}
					}
				}
			}).error(function(error) {
				console.log(error);
			});
	}

	function getDateInStringFormat() {
		var result = {};
		if(vm.startDate === null || isNaN(Date.parse(vm.startDate))) return;
		if(vm.endDate === null || isNaN(Date.parse(vm.endDate))) return;

		result.startDateString = vm.startDate.getFullYear() + '-' 
			+ vm.startDate.getMonthFormatted() + '-' 
			+ vm.startDate.getDateFormatted();

		result.endDateString = vm.endDate.getFullYear() + '-' 
			+ vm.endDate.getMonthFormatted() + '-' 
			+ vm.endDate.getDateFormatted();
		
		return result;
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
