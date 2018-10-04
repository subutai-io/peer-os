'use strict';

angular.module('subutai.tenants.controller', ['ngTagsInput'])
	.controller('TenantsViewCtrl', TenantsViewCtrl);

TenantsViewCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile'];

function TenantsViewCtrl($scope, $rootScope, environmentService, SweetAlert, DTOptionsBuilder, DTColumnBuilder, $resource, $compile) {

	var vm = this;
	vm.deleteEnvironment = deleteEnvironment;

	vm.dtInstance = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( environmentService.getTenantsUrl() ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('stateSave', true)
		.withOption('order', [[ 0, "asc" ]])
		.withOption('createdRow', createdRow);

	vm.dtColumns = [
		DTColumnBuilder.newColumn('name').withTitle('Name'),
		DTColumnBuilder.newColumn('username').withTitle('User').renderWith(actionUsername),
		DTColumnBuilder.newColumn('id').withTitle('ID'),
		DTColumnBuilder.newColumn('dataSource').withTitle('Source'),
		DTColumnBuilder.newColumn('status').withTitle('Status').renderWith(actionStatus),
		DTColumnBuilder.newColumn(null).withTitle('Delete').notSortable().renderWith(actionDelete)
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

    function actionUsername(data, type, full, meta){
        var user = data.split("@");

        if(user.length == 2){
            return "<a href='https://"+ localStorage.getItem("bazaarIp") +"?openPrivateChatWith="+ user[1] +"' target='_blank'>" + user[0] + "<a/>";
        }else{
            return data;
        }
    }

	function actionStatus(data, type, full, meta) {
		var statusString = ['<div class="b-status-icon b-status-icon_' + data + '"',
			'tooltips',
			'tooltip-template="' + data + '"',
			'tooltip-side="right">',
		'</div> ' + data].join('');
		return statusString;
	}

	function actionDelete(data, type, full, meta) {
		return '<a href class="b-icon b-icon_remove" ng-click="tenantsViewCtrl.deleteEnvironment(\'' + data.id + '\',\'' + data.dataSource + '\')"></a>';
	}

	function deleteEnvironment(environmentId, source) {
		var previousWindowKeyDown = window.onkeydown;
		SweetAlert.swal({
				title: "Are you sure?",
				text: "You will not be able to recover this Environment!",
				type: "warning",
				showCancelButton: true,
				confirmButtonColor: "#ff3f3c",
				confirmButtonText: "Delete",
				cancelButtonText: "Cancel",
				closeOnConfirm: false,
				closeOnCancel: true,
				showLoaderOnConfirm: true
			},
			function (isConfirm) {
				window.onkeydown = previousWindowKeyDown;
				if (isConfirm) {
					SweetAlert.swal(
						{
							title : 'Delete!',
							text : 'Your environment is being deleted!!',
							timer: VARS_TOOLTIP_TIMEOUT,
							showConfirmButton: false
						}
					);

					environmentService.destroyEnvironment(environmentId).success(function (data) {
                        if (source == "bazaar"){
                            SweetAlert.swal("Accepted!", "Your environment deletion has been started", "success");
                        }else{
                            SweetAlert.swal("Destroyed!", "Your environment has been destroyed", "success");
                        }
						vm.dtInstance.reloadData(null, false);
					}).error(function (data) {
						$timeout(function() {
							SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
							$rootScope.notificationsUpdate = 'destroyEnvironmentError';
						}, 2000);
					});
				}
			});
	}

}

