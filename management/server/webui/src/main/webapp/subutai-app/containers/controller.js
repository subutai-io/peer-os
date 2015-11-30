'use strict';

angular.module('subutai.containers.controller', ['ngTagsInput'])
	.controller('ContainerViewCtrl', ContainerViewCtrl);

ContainerViewCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', '$stateParams', 'ngDialog'];

function ContainerViewCtrl($scope, environmentService, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, $stateParams, ngDialog) {

	var vm = this;
	vm.environments = [];
	vm.containers = [];
	vm.containersType = [];
	vm.environmentId = $stateParams.environmentId;
	vm.currentTags = [];
	vm.allTags = [];
	vm.tags2Container = {};
	vm.currentDomainStatus = {};
	vm.domainContainer = {};

	// functions
	vm.getContainers = getContainers;
	vm.containerAction = containerAction;
	vm.destroyContainer = destroyContainer;
	vm.addToDomain = addToDomain;
	vm.addTagForm = addTagForm;
	vm.addTags = addTags;
	vm.removeTag = removeTag;
	vm.showDomainForm = showDomainForm;
	vm.checkDomain = checkDomain;

	environmentService.getContainersType().success(function (data) {
		vm.containersType = data;
	});

	function showDomainForm(container) {
		vm.currentDomainStatus = {};
		vm.domainContainer = container;
		environmentService.getContainerDomain(container).success(function (data) {
			vm.currentDomainStatus = data;
			console.log(vm.currentDomainStatus);
		});
		ngDialog.open({
			template: 'subutai-app/containers/partials/addToDomain.html',
			scope: $scope
		});
	}

	function checkDomain() {
		environmentService.checkDomain(vm.domainContainer).success(function (data) {
			vm.currentDomainStatus = data;
		});
		ngDialog.closeAll();
	}

	function getEnvironments() {
		environmentService.getEnvironments().success(function (data) {
			vm.environments = data;
		});
	}

	function addToDomain(container) {
		console.log(container);
	}

	function addTagForm(container) {
		vm.tags2Container = container;
		vm.currentTags = [];
		for(var i = 0; i < container.tags.length; i++) {
			vm.currentTags.push({text: container.tags[i]});
		}
		ngDialog.open({
			template: 'subutai-app/containers/partials/addTagForm.html',
			scope: $scope
		});
	}

	function addTags() {
		var tags = [];
		for(var i = 0; i < vm.currentTags.length; i++){
			tags.push(vm.currentTags[i].text);
		}
		environmentService.setTags(vm.tags2Container.environmentId, vm.tags2Container.id, tags).success(function (data) {
			vm.tags2Container.tags = tags;
			console.log(data);
		});
		vm.tags2Container.tags = tags;
		ngDialog.closeAll();
	}

	function removeTag(container, tag, key) {
		environmentService.removeTag(container.environmentId, container.id, tag).success(function (data) {
			console.log(data);
		});
		container.tags.splice(key, 1);
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
		vm.allTags = [];
		for(var i in vm.environments) {
			if(
				vm.environmentId == vm.environments[i].id || 
				vm.environmentId === undefined || 
				vm.environmentId.length == 0
			) {
				for(var j in vm.environments[i].containers) {
					if(
						vm.containersTypeId !== undefined && 
						vm.containersTypeId != vm.environments[i].containers[j].type && 
						vm.containersTypeId.length > 0
					) {continue;}
					if(
						vm.containerState !== undefined && 
						vm.containerState != vm.environments[i].containers[j].state && 
						vm.containerState.length > 0
					) {continue;}
					vm.containers.push(vm.environments[i].containers[j]);
					vm.allTags = vm.allTags.concat(vm.environments[i].containers[j].tags);
				}
			}
		}
	}

	vm.dtOptions = DTOptionsBuilder
		.newOptions()
		.withOption('order', [[ 2, "asc" ]])
		.withOption('stateSave', true)
		.withPaginationType('full_numbers');
	vm.dtColumnDefs = [
		DTColumnDefBuilder.newColumnDef(0),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2),
		DTColumnDefBuilder.newColumnDef(3).notSortable(),
		DTColumnDefBuilder.newColumnDef(4).notSortable(),
		DTColumnDefBuilder.newColumnDef(5).notSortable(),
		DTColumnDefBuilder.newColumnDef(6).notSortable()
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
