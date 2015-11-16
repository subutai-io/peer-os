'use strict';

angular.module('subutai.blueprints-build.controller', [])
	.controller('BlueprintsBuildCtrl', BlueprintsBuildCtrl)
	.controller('BlueprintsBuildFormCtrl', BlueprintsBuildFormCtrl);

BlueprintsBuildCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'ngDialog', '$stateParams'];
BlueprintsBuildFormCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'ngDialog', '$stateParams'];

function BlueprintsBuildCtrl($scope, environmentService, SweetAlert, ngDialog, $stateParams) {

	var vm = this;
	vm.blueprint = {};
	vm.peers = [];
	vm.strategies = [];
	vm.blueprintAction = $stateParams.action;
	vm.colors = quotaColors;

	vm.nodesToCreate = [];
	vm.transportNodes = [];
	vm.environments = [];
	vm.newEnvironmentName = '';
	vm.environmentToGrow;

	// functions
	vm.placeNode = placeNode;
	vm.removeNode = removeNode;
	vm.buildPopup = buildPopup;

	if(vm.blueprintAction == 'grow') {
		environmentService.getEnvironments().success(function (data) {
			vm.environments = data;
		});
	}

	environmentService.getPeers().success(function (data) {
		vm.peers = data;
	});

	environmentService.getStrategies().success(function (data) {
		vm.strategies = data;
	});

	function buildPopup() {
		var dataToBuild = {};

		if(vm.nodesToCreate.length < 1) return;

		dataToBuild.nodesToCreate = vm.nodesToCreate;
		if(vm.blueprintAction == 'build') {
			dataToBuild.newEnvironmentName = vm.newEnvironmentName;
		} else if(vm.blueprintAction == 'grow') {
			dataToBuild.environmentToGrow = vm.environmentToGrow;
		}		

		ngDialog.open({
			template: 'subutai-app/blueprintsBuild/partials/buildPopup.html',
			controller: 'BlueprintsBuildFormCtrl',
			controllerAs: 'blueprintsBuildFormCtrl',
			data: dataToBuild,
			preCloseCallback: function(value) {
				//callback
			}
		});
	}

	function getCurrentBlueprint() {
		environmentService.getBlueprintById($stateParams.blueprintId).success(function (data) {
			vm.blueprint = data;

			for(var i = 0; i < vm.blueprint.nodeGroups.length; i++) {
				vm.transportNodes[i] = {};
				vm.transportNodes[i].name = vm.blueprint.nodeGroups[i].name;
				vm.transportNodes[i].numberOfContainers = vm.blueprint.nodeGroups[i].numberOfContainers;
				vm.transportNodes[i].disabled = false;
				vm.transportNodes[i].options = {
					start: vm.transportNodes[i].numberOfContainers, 
					range: {
						min: 1, 
						max: vm.transportNodes[i].numberOfContainers
					}, 
					step: 1,
					tooltips: true,
					format: wNumb({
						decimals: 0,
					}),
					pips: {
						mode: "count", 
						values: vm.transportNodes[i].numberOfContainers, 
						density: 1
					}
				};
			}
		});
	}
	getCurrentBlueprint();

	function placeNode(node, nodeGroup, key) {
		if(node.peer === undefined) return;
		if(node.strategyId === undefined) return;
		var foundedInArray = false;
		for(var i = 0; i < vm.nodesToCreate.length; i++) {
			if(vm.nodesToCreate[i].peer == node.peer && vm.nodesToCreate[i].name == node.name) {
				vm.nodesToCreate[i].peer = node.peer;
				vm.nodesToCreate[i].strategyId = node.strategyId;
				vm.nodesToCreate[i].numberOfContainers = (
					parseInt(vm.nodesToCreate[i].numberOfContainers) + parseInt(node.options.start)
				);
				foundedInArray = true;
				break;
			}
		}

		if(!foundedInArray) {
			var copyNode = {};
			copyNode.name = node.name;
			copyNode.numberOfContainers = parseInt(node.options.start);
			copyNode.peer = node.peer;
			copyNode.strategyId = node.strategyId;
			copyNode.parentNode = key;
			vm.nodesToCreate.push(copyNode);
		}

		nodeGroup.numberOfContainers = nodeGroup.numberOfContainers - node.options.start;
		if(nodeGroup.numberOfContainers > 0) {
			node.numberOfContainers = nodeGroup.numberOfContainers;
			node.options.start = nodeGroup.numberOfContainers;
			node.options.min = nodeGroup.numberOfContainers;
			node.options.pips.values = nodeGroup.numberOfContainers;
			node.options.range.max = nodeGroup.numberOfContainers;
			node.disabled = false;
		} else {
			node.options.start = 0;
			node.numberOfContainers = 0;
			node.disabled = true;
			nodeGroup.numberOfContainers = 0;
		}
	}

	function removeNode(key) {
		var parentKey = vm.nodesToCreate[key].parentNode;

		var numberOfContainers = (
			parseInt(vm.blueprint[parentKey].numberOfContainers) 
			+ 
			parseInt(vm.nodesToCreate[key].numberOfContainers)
		);

		vm.blueprint[parentKey].numberOfContainers = numberOfContainers;
		vm.nodesToCreate.splice(key, 1);
	}

}

function BlueprintsBuildFormCtrl($scope, environmentService, SweetAlert, ngDialog, $stateParams) {

	var vm = this;
	vm.blueprint = {};

	vm.blueprintAction = $stateParams.action;
	vm.nodesToCreate = [];
	vm.environments = [];
	vm.newEnvironmentName = '';
	vm.environmentToGrow;	

	vm.groupList = {};
	vm.colors = quotaColors;

	//functions
	vm.start = start;	

	if($scope.ngDialogData !== undefined) {
		vm.nodesToCreate = $scope.ngDialogData.nodesToCreate;

		for(var i = 0; i < vm.nodesToCreate.length; i++) {
			var currentNode = angular.copy(vm.nodesToCreate[i]);
			if(vm.groupList[currentNode.peer] === undefined) {
				vm.groupList[currentNode.peer] = {};
				if(vm.groupList[currentNode.peer][currentNode.strategyId] === undefined) {
					vm.groupList[currentNode.peer][currentNode.strategyId] = [];
				}
			}
			vm.groupList[currentNode.peer][currentNode.strategyId].push(currentNode);
		}

		if(vm.blueprintAction == 'build') {
			vm.newEnvironmentName = $scope.ngDialogData.newEnvironmentName;
		} else if(vm.blueprintAction == 'grow') {
			vm.environmentToGrow = $scope.ngDialogData.environmentToGrow;
		}
	}

	environmentService.getBlueprintById($stateParams.blueprintId).success(function (data) {
		vm.blueprint = data;
	});

	function getNodesGroups() {
		var nodeGroupsArray = [];
		for(var i = 0; i < vm.nodesToCreate.length; i++) {

			var currentNodeGroup = vm.blueprint.nodeGroups[vm.nodesToCreate[i].parentNode];
			var containerPlacementStrategy = {"strategyId": vm.nodesToCreate[i].strategyId, "criteria": []};

			currentNodeGroup.numberOfContainers = vm.nodesToCreate[i].numberOfContainers;
			currentNodeGroup.containerPlacementStrategy = containerPlacementStrategy;
			currentNodeGroup.peerId = vm.nodesToCreate[i].peer;
			currentNodeGroup.containerDistributionType = 'AUTO';

			nodeGroupsArray.push(currentNodeGroup);
		}
		return nodeGroupsArray;
	}

	function start() {
		if(vm.blueprintAction == 'build') {
			buildBlueprint();
		} else if(vm.blueprintAction == 'grow') {
			growBlueprint();
		}
	}

	function buildBlueprint(){
		if(vm.newEnvironmentName === undefined || vm.newEnvironmentName.length < 1) return;

		var postJson = {};
		postJson.nodeGroups = getNodesGroups();
		postJson.name = vm.newEnvironmentName;

		var postData = JSON.stringify(postJson);

		SweetAlert.swal("Success!", "Your environment start creation.", "success");
		environmentService.buildBlueprint(encodeURI(postData)).success(function (data) {
			SweetAlert.swal("Success!", "Your environment has been created.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Create environment error: ' + error.ERROR, "error");
		});
	}

	function growBlueprint() {
		if(vm.environmentToGrow === undefined) return;
		var postJson = {};
		//postJson.environmentId = vm.environmentToGrow;
		postJson.name = '';
		postJson.nodeGroups = getNodesGroups();
		var postData = JSON.stringify(postJson);		

		SweetAlert.swal("Success!", "Your environment start growing.", "success");
		environmentService.growBlueprint(vm.environmentToGrow, encodeURI(postData)).success(function (data) {
			SweetAlert.swal("Success!", "You successfully grow environment.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Grow environment error: ' + error.ERROR, "error");
		});
	}
	
}

