'use strict';

angular.module('subutai.containers.controller', ['ngTagsInput'])
	.controller('ContainerViewCtrl', ContainerViewCtrl);

ContainerViewCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', '$stateParams', 'ngDialog', '$timeout'];

function ContainerViewCtrl($scope, $rootScope, environmentService, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, $stateParams, ngDialog, $timeout) {

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
	vm.addTagForm = addTagForm;
	vm.addTags = addTags;
	vm.removeTag = removeTag;
	vm.showDomainForm = showDomainForm;
	vm.checkDomain = checkDomain;

	environmentService.getContainersType().success(function (data) {
		vm.containersType = data;
	});

	function showDomainForm(container) {
		LOADING_SCREEN();
		vm.currentDomainStatus = {};
		vm.domainContainer = container;
		environmentService.getContainerDomain(container).success(function (data) {
			vm.currentDomainStatus = data;
			LOADING_SCREEN('none');
			console.log(vm.currentDomainStatus);
		}).error(function(error){
			LOADING_SCREEN('none');
			ngDialog.closeAll();
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
		environmentService.getEnvironments().success(function (data) {

			for(var i = 0; i < data.length; i++) {
				data[i].containers.sort(compare);
			}
			data.sort(compare);

			var currentArrayString = JSON.stringify(vm.environments, function( key, value ) {
				if( key === "$$hashKey" ) {
					return undefined;
				}
				return value;
			});
			var serverArrayString = JSON.stringify(data, function( key, value ) {
				if( key === "$$hashKey" ) {
					return undefined;
				}
				return value;
			});

			if(currentArrayString != serverArrayString) {
				vm.environments = data;
				filterContainersList();
			}
		});
	}
	getContainers();

	function compare(a,b) {
		if (a.id < b.id) return -1;
		if (a.id > b.id) return 1;
		return 0;
	}

	function filterContainersList() {
		vm.allTags = [];
		vm.containers = [];
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

	var refreshTable;
	var reloadTableData = function() {
		refreshTable = $timeout(function myFunction() {
			getContainers();
			refreshTable = $timeout(reloadTableData, 30000);
		}, 30000);
	};
	reloadTableData();

	$rootScope.$on('$stateChangeStart',	function(event, toState, toParams, fromState, fromParams){
		console.log('cancel');
		$timeout.cancel(refreshTable);
	});

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
			/*environmentService.getContainerStatus(vm.containers[key].id).success(function (data) {
				vm.containers[key].state = data.STATE;
			});*/
		});		
	}

}
