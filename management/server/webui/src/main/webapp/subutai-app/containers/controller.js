'use strict';

angular.module('subutai.containers.controller', [])
	.controller('ContainerViewCtrl', ContainerViewCtrl);

ContainerViewCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', '$stateParams'];

function ContainerViewCtrl($scope, environmentService, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, $stateParams) {

	var vm = this;
	vm.environments = [];
	vm.containers = [];
	vm.containersType = [];
	vm.environmentId = $stateParams.environmentId;

	// functions
	vm.getContainers = getContainers;
	vm.containerAction = containerAction;
	vm.destroyContainer = destroyContainer;

	environmentService.getContainersType().success(function (data) {
		vm.containersType = data;
	});

	function getEnvironments() {
		environmentService.getEnvironments().success(function (data) {
			vm.environments = data;
		});
	}

	function getContainers() {
		vm.containers = [];
		if(vm.environments.length < 1){
			environmentService.getEnvironments().success(function (data) {
				vm.environments = data;
				filterContainersList();
			});
		} else {
			filterContainersList();
		}
	}
	getContainers();

	function filterContainersList() {
		for(var i in vm.environments) {
			if(
				vm.environmentId == vm.environments[i].id || 
				vm.environmentId === undefined || 
				vm.environmentId.length == 0
			) {
				for(var j in vm.environments[i].containers) {
					if(
						vm.containersTypeId === undefined || 
						vm.containersTypeId == vm.environments[i].containers[j].type
					) {
						vm.containers.push(vm.environments[i].containers[j]);
					}
				}
			}
		}
	}

	vm.dtOptions = DTOptionsBuilder
		.newOptions()
		//.withOption('order', [[ 1, "asc" ]])
		.withOption('stateSave', true)
		.withPaginationType('full_numbers');
	vm.dtColumnDefs = [
		DTColumnDefBuilder.newColumnDef(0),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2).notSortable(),
		DTColumnDefBuilder.newColumnDef(3),
		DTColumnDefBuilder.newColumnDef(4).notSortable(),
		DTColumnDefBuilder.newColumnDef(5).notSortable()
	];	

	function destroyContainer(containerId, key) {
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
					vm.containers.splice(key, 1);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function containerAction(key) {
		var action = 'start';
		if(vm.containers[key].state == 'RUNNING') {
			action = 'stop';
			vm.containers[key].state = 'STOPPING';
		} else {
			vm.containers[key].state = 'STARTING';
		}

		environmentService.switchContainer(vm.containers[key].id, action).success(function (data) {
			environmentService.getContainerStatus(vm.containers[key].id).success(function (data) {
				vm.containers[key].state = data.STATE;
			});				
		});		
	}

}
