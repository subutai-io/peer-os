"use strict";

angular.module("subutai.settings-updates.controller", [])
.controller("SettingsUpdatesCtrl", SettingsUpdatesCtrl);


SettingsUpdatesCtrl.$inject = ['$scope', 'SettingsUpdatesSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile'];

function SettingsUpdatesCtrl($scope, SettingsUpdatesSrv, SweetAlert, DTOptionsBuilder, DTColumnBuilder, $resource, $compile) {
	var vm = this;
	vm.config = {isUpdatesAvailable: "waiting"};
	vm.getHistory = [];
	vm.activeTab = 'update';
	vm.updateText = 'Checking...';

	function getConfig() {
		LOADING_SCREEN();
		SettingsUpdatesSrv.getConfig().success(function (data) {
			LOADING_SCREEN('none');
			vm.config = data;
			if(vm.config.isUpdatesAvailable == true) {
				vm.updateText = 'Update is available';
			} else {
				vm.updateText = 'Your system is already up-to-date';
			}
		}).error(function(error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", error, "error");
		});
	}

	getConfig();

	vm.dtInstance = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( SettingsUpdatesSrv.getHistoryUrl() ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('stateSave', true)
		//.withOption('order', [[ 0, "desc" ]])
		.withOption('createdRow', createdRow);

	vm.dtColumns = [
		DTColumnBuilder.newColumn('updateDate').withTitle('Date').renderWith(dateHTML),
		DTColumnBuilder.newColumn('prevVersion').withTitle('Previous version'),
		DTColumnBuilder.newColumn('currentVersion').withTitle('Current version'),
		DTColumnBuilder.newColumn('prevCommitId').withTitle('Previous Commit Id'),
		DTColumnBuilder.newColumn('currentCommitId').withTitle('Current commit Id')
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function dateHTML(data, type, full, meta) {
		return moment( data ).format('MMM Do YYYY HH:mm:ss');
	}

	vm.update = update;
	vm.getHistoryData = getHistoryData;

	function getHistoryData() {
		LOADING_SCREEN();
		SettingsUpdatesSrv.getHistory().success(function (data) {
			LOADING_SCREEN('none');
			vm.getHistory = data;
			console.log(vm.getHistory);
		}).error(function(error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", error, "error");
		});
	}
	getHistoryData();

	function update() {

		LOADING_SCREEN();
		vm.updateText = 'Please wait, update is in progress. System will restart automatically';
		SettingsUpdatesSrv.update(vm.config).success(function (data) {
			LOADING_SCREEN('none');
			sessionStorage.removeItem('notifications');
			SweetAlert.swal("Success!", "Subutai Successfully updated.", "success");
			getConfig();
		}).error(function (error) {
			//LOADING_SCREEN('none');
			//SweetAlert.swal("ERROR!", "Save config error: " + error, "error");
			//getConfig();
			setInterval(function() {
				update();
			}, 120000);
		});

		var notifications = sessionStorage.getItem('notifications');
		if (
			notifications !== null &&
			notifications !== undefined &&
			notifications !== 'null' &&
			notifications.length > 0
		) {
			notifications = JSON.parse(notifications);
			for (var i = 0; i < notifications.length; i++) {
				if (notifications[i].updateMessage !== undefined && notifications[i].updateMessage) {
					notifications.splice(i, 1);
					sessionStorage.setItem('notifications', JSON.stringify(notifications));
					$rootScope.notifications = {};
					break;
				}
			}
		}

	}
}
