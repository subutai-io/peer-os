'use strict';

angular.module('subutai.environment.controller', [])
	.controller('EnvironmentViewCtrl', EnvironmentViewCtrl);

EnvironmentViewCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile',];

function EnvironmentViewCtrl($scope, environmentService, SweetAlert, DTOptionsBuilder, DTColumnBuilder, $resource, $compile) {

	var vm = this;
	vm.environments = [];

	// functions
	vm.destroyEnvironment = destroyEnvironment;
	vm.sshKey = sshKey;
	vm.addSshKey = addSshKey;
	vm.removeSshKey = removeSshKey;
	vm.getEnvironments = getEnvironments;
	vm.showContainersList = showContainersList;
	vm.destroyContainer = destroyContainer;

	function getEnvironments() {
		environmentService.getEnvironments().success(function (data) {
			vm.environments = data;
		});
	}

	getEnvironments();

	vm.dtInstance = {};
	vm.users = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource(serverUrl + 'environments_ui/').query().$promise;
		}).withPaginationType('full_numbers')
		.withOption('createdRow', createdRow)
		.withOption('order', [[ 1, "asc" ]])
		//.withDisplayLength(2)
		.withOption('stateSave', true);

	vm.dtColumns = [
		//DTColumnBuilder.newColumn('id').withTitle('ID'),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(statusHTML),
		DTColumnBuilder.newColumn('name').withTitle('Environment name'),
		DTColumnBuilder.newColumn(null).withTitle('Key SSH').renderWith(sshKeyLinks),
		//DTColumnBuilder.newColumn(null).withTitle('Domains'),
		DTColumnBuilder.newColumn(null).withTitle('').renderWith(containersTags),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionDelete)
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function statusHTML(data, type, full, meta) {
		vm.users[data.id] = data;
		return '<div class="b-status-icon b-status-icon_' + data.status + '" title="' + data.status + '"></div>';
	}

	function sshKeyLinks(data, type, full, meta) {
		var addSshKeyLink = '<a href ng-click="environmentViewCtrl.addSshKey(\'' + data.id + '\')">Add</a>';
		var removeSshKeyLink = '<a href ng-click="environmentViewCtrl.removeSshKey(\'' + data.id + '\')">Remove</a>';
		return addSshKeyLink + '/' + removeSshKeyLink;
	}

	function containersTags(data, type, full, meta) {
		var containersHTML = '';
		for(var i = 0; i < data.containers.length; i++) {
			containersHTML += '<span class="b-tags b-tags_' + quotaColors[data.containers[i].type] + '">' 
				+ '<a ui-sref="containers({environmentId:\'' + data.id + '\'})">' + data.containers[i].templateName + '</a>' 
				+ ' <a href ng-click="environmentViewCtrl.destroyContainer(\'' + data.containers[i].id + '\')"><i class="fa fa-times"></i></a>' 
			+ '</span>';
		}
		return containersHTML;
	}

	function actionDelete(data, type, full, meta) {
		return '<a href="" class="b-icon b-icon_remove" ng-click="environmentViewCtrl.destroyEnvironment(\'' + data.id + '\')"></a>';
	}

	function destroyContainer(containerId) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this Container!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Destroy",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {
				environmentService.destroyContainer(containerId).success(function (data) {
					SweetAlert.swal("Destroyed!", "Your container has been destroyed.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function destroyEnvironment(environmentId) {
		SweetAlert.swal({
				title: "Are you sure?",
				text: "Your will not be able to recover this Environment!",
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
				if (isConfirm) {
					SweetAlert.swal("Delete!", "Your environment start deleting!", "success");
					environmentService.destroyEnvironment(environmentId).success(function (data) {
						SweetAlert.swal("Destroyed!", "Your environment has been destroyed.", "success");
						vm.dtInstance.reloadData(null, false);
					}).error(function (data) {
						SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
					});
					vm.dtInstance.reloadData(null, false);
				}
			});
	}

	function showContainersList(key) {
		vm.containers = vm.environments[key].containers;
	}

	function sshKey(key) {
		delete vm.enviromentSSHKey.key;
		vm.enviromentSSHKey.environmentKey = key;
	}

	function addSshKey(key){
		var enviroment = vm.environments[vm.enviromentSSHKey.environmentKey];
		environmentService.addSshKey(vm.enviromentSSHKey.key, enviroment.id).success(function (data) {
			SweetAlert.swal("Success!", "You successfully add SSH key for " + enviroment.id + " environment!", "success");
			console.log(data);
		}).error(function (data) {
			SweetAlert.swal("Cancelled", "Error: " + data.ERROR, "error");
			console.log(data);
		});
	}

	function removeSshKey(environmentId){
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Delete environment SSH keys!",
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
			if (isConfirm) {
				environmentService.removeSshKey(environmentId).success(function () {
					SweetAlert.swal("Destroyed!", "Your enviroment SSH keys has been deleted.", "success");
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your SSH keys is safe :). Error: " + data.ERROR, "error");
				});
			}
		});
	}	

	function getContainers() {
		var environment = vm.environments[vm.environmentQuota];
		vm.containersForQuota = environment.containers;
	}

}
