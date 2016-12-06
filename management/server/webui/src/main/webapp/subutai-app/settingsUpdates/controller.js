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
	vm.updateInProgress = false;

	checkActiveUpdate();
    getHistoryData();

	function checkActiveUpdate(){

        LOADING_SCREEN();

	    SettingsUpdatesSrv.isUpdateInProgress().success(function (data){
          	 if (data == true || data == 'true') {
	            LOADING_SCREEN("none");

          	    vm.updateInProgress = true;
          	    vm.updateText = 'Update is in progress';

                setTimeout(function() {checkActiveUpdate();}, 30000);
          	 }else{
          	    getConfig();
          	 }
	    });
	}


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


	vm.dtInstance = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( SettingsUpdatesSrv.getHistoryUrl() ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('createdRow', createdRow)
		.withOption('order', [[ 0, "desc" ]])
		.withOption('stateSave', true);

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

	/*function dateHTML(data, type, full, meta) {
		return moment( data ).format('MMM Do YYYY HH:mm:ss');
	}*/

	function dateHTML(data, type, full, meta) {
		return '<div>' + moment( data ).format('MMM Do YYYY HH:mm:ss') + '</div>';
	}	

	vm.update = update;
	vm.getHistoryData = getHistoryData;

	function getHistoryData() {
		SettingsUpdatesSrv.getHistory().success(function (data) {
			vm.getHistory = data;
			console.log(vm.getHistory);
		}).error(function(error) {
			SweetAlert.swal("ERROR!", error, "error");
		});
	}



	function update() {

		LOADING_SCREEN();
		vm.updateText = 'Please wait, update is in progress. System will restart automatically';

		SettingsUpdatesSrv.update(vm.config).success(function (data, status) {
			LOADING_SCREEN('none');
			sessionStorage.removeItem('notifications');
			if(status == 200){
			    SweetAlert.swal("Success!", "Subutai Successfully updated.", "success");
			}
			checkActiveUpdate();
		}).error(function (error) {
			SweetAlert.swal("ERROR!", error, "error");
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
