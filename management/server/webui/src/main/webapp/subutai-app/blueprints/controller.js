'use strict';

angular.module('subutai.blueprints.controller', [])
	.controller('BlueprintsViewCtrl', BlueprintsViewCtrl)
	.controller('CreateBlueprintCtrl', CreateBlueprintCtrl);

BlueprintsViewCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'ngDialog'];
CreateBlueprintCtrl.$inject = ['$scope', 'environmentService', 'ngDialog'];

function BlueprintsViewCtrl($scope, environmentService, SweetAlert, ngDialog) {

	var vm = this;
	vm.blueprints = [];
	vm.peers = [];

	vm.colors = quotaColors;

	// functions
	vm.createBlueprintFrom = createBlueprintFrom;
	vm.deleteBlueprint = deleteBlueprint;

	function getBlueprints() {
		environmentService.getBlueprints().success(function (data) {
			vm.blueprints = data;
		});
	}
	getBlueprints();

	function deleteBlueprint(blueprintId, key){
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this blueprint!",
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
				SweetAlert.swal("Deleted!", "Your blueprint has been deleted.", "success");
				environmentService.deleteBlueprint(blueprintId).success(function (data) {
					vm.blueprints.splice(key, 1);
				});
			}
		});
	}

	function createBlueprintFrom(key) {
		if(key === undefined || key === null) key = false;

		var dataToForm;
		if(key !== false) {
			dataToForm = angular.copy(vm.blueprints[key]);
		}

		ngDialog.open({
			template: 'subutai-app/blueprints/partials/blueprintForm.html',
			controller: 'CreateBlueprintCtrl',
			controllerAs: 'createBlueprintCtrl',
			data: dataToForm,
			preCloseCallback: function(value) {
				getBlueprints();
			}
		});
	}

}

function CreateBlueprintCtrl($scope, environmentService, ngDialog) {
	
	var vm = this;

	vm.blueprintFrom = {};
	vm.blueprintFrom.currentNode = getDefaultValues();
	vm.nodeList = [];	
	vm.templates = [];
	vm.containersType = [];

	environmentService.getTemplates().success(function (data) {
		vm.templates = data;
	});

	environmentService.getContainersType().success(function (data) {
		vm.containersType = data;
	});

	if($scope.ngDialogData !== undefined) {
		vm.blueprintFrom = $scope.ngDialogData;
		vm.nodeList = angular.copy(vm.blueprintFrom.nodeGroups);
		vm.blueprintFrom.nodeGroups = [];
	}

	vm.nodeStatus = 'Add to';
	vm.addBlueprintType = 'build';	

	//functions
	vm.addBlueprint = addBlueprint;
	vm.addNewNode = addNewNode;
	vm.setNodeData = setNodeData;

	function addNewNode() {
		if(vm.nodeStatus == 'Add to') {
			var tempNode = vm.blueprintFrom.currentNode;

			if(tempNode === undefined) return;
			if(tempNode.name === undefined || tempNode.name.length < 1) return;
			if(tempNode.numberOfContainers === undefined || tempNode.numberOfContainers < 1) return;
			if(tempNode.sshGroupId === undefined) return;
			if(tempNode.hostsGroupId === undefined) return;

			vm.nodeList.push(tempNode);
		} else {
			vm.nodeStatus = 'Add to';
		}
		vm.blueprintFrom.currentNode = getDefaultValues();
	}

	function setNodeData(key) {
		vm.nodeStatus = 'Update in';
		vm.blueprintFrom.currentNode = vm.nodeList[key];
	}	

	function addBlueprint() {
		if(vm.blueprintFrom.name === undefined) return;
		if(vm.nodeList === undefined || vm.nodeList.length == 0) return;

		var finalBlueprint = vm.blueprintFrom;
		finalBlueprint.nodeGroups = vm.nodeList;
		if(finalBlueprint.currentNod !== undefined) {
			finalBlueprint.nodeGroups.push(finalBlueprint.currentNode);
		}
		delete finalBlueprint.currentNode;

		environmentService.createBlueprint(JSON.stringify(finalBlueprint)).success(function (data) {
			ngDialog.closeAll();
		});

		vm.nodeList = [];
		vm.blueprintFrom = {};
	}

	function getDefaultValues() {
		var defaultVal = {
			"templateName": "master",
			"numberOfContainers": 2,
			"sshGroupId": 0,
			"hostsGroupId": 0,
			"type": "SMALL"
		};
		return defaultVal;
	}
	
}
