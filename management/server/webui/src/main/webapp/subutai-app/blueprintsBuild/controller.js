'use strict';

angular.module('subutai.blueprints-build.controller', [])
	.controller('BlueprintsBuildCtrl', BlueprintsBuildCtrl);

BlueprintsBuildCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'ngDialog', '$stateParams', '$location'];

function BlueprintsBuildCtrl($scope, environmentService, SweetAlert, ngDialog, $stateParams, $location) {

	var vm = this;
	vm.blueprint = {};
	vm.peers = [];
	vm.buildTypes = [];
	vm.blueprintAction = $stateParams.action;
	vm.colors = quotaColors;

	vm.buildWith = 'strategie';

	vm.nodesToCreate = [];
	vm.transportNodes = [];
	vm.environments = [];
	vm.newEnvironmentName = '';
	vm.environmentToGrow;
	vm.totalContainers = 0;
	vm.popupError = false;

	vm.groupList = {};
	vm.colors = quotaColors;	

	// functions
	vm.placeNode = placeNode;
	vm.buildPopup = buildPopup;

	//popup functions
	vm.start = start;	
	vm.removeNode = removeNode;
	vm.removeGroup = removeGroup;	

	environmentService.getBlueprintById($stateParams.blueprintId).success(function (data) {
		vm.blueprint = data;

		for(var i = 0; i < vm.blueprint.nodeGroups.length; i++) {
			vm.transportNodes[i] = angular.copy(vm.blueprint.nodeGroups[i]);
			vm.transportNodes[i].show = true;
			vm.transportNodes[i].disabled = true;

			var minSlider = 1;
			if(vm.transportNodes[i].numberOfContainers == 1) {
				minSlider = 0;
			}

			vm.transportNodes[i].options = {
				start: vm.transportNodes[i].numberOfContainers, 
				range: {
					min: minSlider, 
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

	if(vm.blueprintAction == 'grow') {
		environmentService.getEnvironments().success(function (data) {
			vm.environments = data;
		});
	}

	function getPeers() {
		LOADING_SCREEN();
		environmentService.getPeers().success(function (data) {
			vm.peers = data;
			LOADING_SCREEN('none');
			environmentService.getStrategies().success(function (strategie) {
				for(var i in vm.peers) {
					var resources = vm.peers[i];
					vm.peers[i] = {"strategie": strategie, "resources": resources};
				}
			});
		});
	}
	getPeers();

	function buildPopup() {

		if(vm.blueprintAction == 'grow') {
			if(vm.environmentToGrow === undefined || vm.environmentToGrow.length < 1) {
				vm.popupError = true;
				return;
			}
		} else {
			if(vm.newEnvironmentName === undefined || vm.newEnvironmentName.length < 1) {
				vm.popupError = true;
				return;
			}
		}

		if(vm.nodesToCreate.length < 1) return;
		vm.popupError = false;

		for(var i = 0; i < vm.nodesToCreate.length; i++) {
			var currentNode = vm.nodesToCreate[i];

			if(currentNode !== null) {
				if(vm.groupList[currentNode.peer] === undefined) {
					vm.groupList[currentNode.peer] = {};
				}
				if(vm.groupList[currentNode.peer][currentNode.createOption] === undefined) {
					vm.groupList[currentNode.peer][currentNode.createOption] = [];
				}

				currentNode.nodesToCreateKey = i;
				vm.groupList[currentNode.peer][currentNode.createOption].push(currentNode);
			}
		}

		ngDialog.open({
			template: 'subutai-app/blueprintsBuild/partials/buildPopup.html',
			scope: $scope,
			preCloseCallback: function(value) {
				vm.groupList = {};
			}
		});
	}

	function placeNode(node, parentKey) {
		if(node.peer === undefined) return;
		if(node.createOption === undefined) return;
		if(node.options.start < 1) return;

		var key = findContainer(node);

		if(key !== false) {
			vm.nodesToCreate[key].numberOfContainers = vm.nodesToCreate[key].numberOfContainers + node.options.start;			
		} else {
			var temp = angular.copy(node);
			temp.parentNode = parentKey;
			temp.numberOfContainers = node.options.start;			
			vm.nodesToCreate.push(temp);
		}

		vm.totalContainers +=  node.options.start;
		node.numberOfContainers = node.numberOfContainers - node.options.start;
		node = setRangeSliderValues(node, node.numberOfContainers);
	}

	function setRangeSliderValues(node, value) {
		if(value > 0) {
			node.options.range.max = value;
			node.options.pips.values = value;
			node.show = true;
		} else {
			node.show = false;
		}
		node.numberOfContainers = value;
		node.options.start = value;
		node.options.min = value;
	}

	function findContainer(node) {
		for(var i = 0; i < vm.nodesToCreate.length; i++) {
			if(vm.nodesToCreate[i] !== null){
				var currentNode = vm.nodesToCreate[i];
				if(
					currentNode.peer == node.peer && 
					currentNode.name == node.name && 
					currentNode.templateName == node.templateName
				) {
					return i;
				}
			}
		}
		return false;
	}

	function removeNodeFromCreateList(key) {
		var parentKey = vm.nodesToCreate[key].parentNode;
		var node = vm.transportNodes[parentKey];

		var numberOfContainers = (
			parseInt(node.numberOfContainers) 
			+ 
			parseInt(vm.nodesToCreate[key].numberOfContainers)
		);

		vm.totalContainers = vm.totalContainers - vm.nodesToCreate[key].numberOfContainers;
		node.numberOfContainers = numberOfContainers;
		node = setRangeSliderValues(node, numberOfContainers);

		vm.nodesToCreate[key] = null;
	}

	function removeNode(peer, strategies, itemKey, itemParentKey) {
		removeNodeFromCreateList(itemParentKey);
		vm.groupList[peer][strategies].splice(itemKey, 1);
	}

	function removeGroup(peer, strategies) {
		for(var i = 0; i < vm.groupList[peer][strategies].length; i++) {
			removeNodeFromCreateList(vm.groupList[peer][strategies][i].nodesToCreateKey);
		}
		delete vm.groupList[peer][strategies];
	}

	function getNodesGroups() {
		var nodeGroupsArray = [];
		for(var i = 0; i < vm.nodesToCreate.length; i++) {

			if(vm.nodesToCreate[i] !== null) {
				var currentNodeGroup = angular.copy(vm.blueprint.nodeGroups[vm.nodesToCreate[i].parentNode]);

				if(vm.buildWith == 'strategie') {
					var containerPlacementStrategy = {"strategyId": vm.nodesToCreate[i].createOption, "criteria": []};
					currentNodeGroup.containerPlacementStrategy = containerPlacementStrategy;
				} else {
					currentNodeGroup.hostId = vm.nodesToCreate[i].createOption;
				}

				currentNodeGroup.numberOfContainers = vm.nodesToCreate[i].numberOfContainers;
				currentNodeGroup.peerId = vm.nodesToCreate[i].peer;
				currentNodeGroup.containerDistributionType = 'AUTO';

				nodeGroupsArray.push(currentNodeGroup);
				currentNodeGroup = {};
			}
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
		ngDialog.closeAll();
		$location.path('/environments');

		environmentService.createEnvironment(encodeURI(postData)).success(function (data) {
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
		ngDialog.closeAll();
		$location.path('/environments');

		environmentService.growEnvironment(vm.environmentToGrow, encodeURI(postData)).success(function (data) {
			SweetAlert.swal("Success!", "You successfully grow environment.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Grow environment error: ' + error.ERROR, "error");
		});
	}

}

