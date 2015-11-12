'use strict';

angular.module('subutai.environment.controller', [])
	.controller('EnvironmentViewCtrl', EnvironmentViewCtrl);

EnvironmentViewCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert'];

function EnvironmentViewCtrl($scope, environmentService, SweetAlert) {

	var vm = this;
	vm.blueprints = {};
	vm.peers = [];
	vm.templates = [];
	vm.environments = [];

	vm.blueprintFrom = {};
	vm.blueprintFrom.currentNode = {};
	vm.nodeList = [];
	vm.createEnviromentInfo = [];
	vm.nodesToCreate = [];
	vm.transportNodes = [];
	vm.subnetCIDR = '192.168.10.1/24';
	vm.currentBlueprint;
	vm.enviromentSSHKey = {};
	vm.environmentToGrow;
	vm.containers = [];
	vm.containersForQuota = [];
	vm.envQuota = {};

	vm.nodeStatus = 'Add to';
	vm.addBlueprintType = 'build';

	// functions
	vm.addBlueprint = addBlueprint;
	vm.addNewNode = addNewNode;
	vm.setNodeData = setNodeData;
	vm.deleteBlueprint = deleteBlueprint;
	vm.addPanel = addPanel;
	vm.showBlueprintCreateBlock = showBlueprintCreateBlock;
	vm.placeNode = placeNode;
	vm.removeNode = removeNode;
	vm.buildBlueprint = buildBlueprint;
	vm.growBlueprint = growBlueprint;
	vm.destroyEnvironment = destroyEnvironment;
	vm.sshKey = sshKey;
	vm.addSshKey = addSshKey;
	vm.removeSshKey = removeSshKey;
	vm.getEnvironments = getEnvironments;
	vm.showContainersList = showContainersList;
	vm.containerAction = containerAction;
	vm.destroyContainer = destroyContainer;
	vm.getContainers = getContainers;
	vm.contChanged = contChanged;
	vm.updateQuota = updateQuota;	

	environmentService.getBlueprints().success(function (data) {
		vm.blueprints = data;
	});

	environmentService.getTemplates().success(function (data) {
		vm.templates = data;
	});

	environmentService.getPeers().success(function (data) {
		vm.peers = data;
	});

	$scope.closePanel = closePanel;

	function getEnvironments() {
		environmentService.getEnvironments().success(function (data) {
			vm.environments = data;
		});		
	}

	getEnvironments();

	function deleteBlueprint(blueprintId, key){
		SweetAlert.swal(
			{
				title: "Are you sure?",
				text: "Your will not be able to recover this blueprint!",
				type: "warning",
				showCancelButton: true,
				confirmButtonColor: "#DD6B55",
				confirmButtonText: "Yes, delete it!",
				cancelButtonText: "No, cancel plx!",
				closeOnConfirm: false,
				closeOnCancel: false,
				showLoaderOnConfirm: true
			},
			function (isConfirm) {
				if (isConfirm) {
					SweetAlert.swal("Deleted!", "Your blueprint has been deleted.", "success");
					environmentService.deleteBlueprint(blueprintId).success(function (data) {
						vm.blueprints.splice(key, 1);
					});
				} else {
					SweetAlert.swal("Cancelled", "Your blueprint is safe :)", "error");
				}
			}
		);
	}

	function showBlueprintCreateBlock(key, type) {
		vm.addBlueprintType = type;
		vm.createEnviromentInfo = angular.copy(vm.blueprints[key].nodeGroups);
		vm.currentBlueprint = angular.copy(vm.blueprints[key]);
		for(var i = 0; i < vm.blueprints[key].nodeGroups.length; i++) {
			vm.transportNodes[i] = {};
			vm.transportNodes[i].name = vm.blueprints[key].nodeGroups[i].name;
			vm.transportNodes[i].numberOfContainers = vm.blueprints[key].nodeGroups[i].numberOfContainers;
		}
		vm.nodesToCreate = [];
		vm.subnetCIDR = '192.168.10.1/24';
		addPanel('buildBlueprint');
	}

	function placeNode(node, nodeGroup, key) {
		if(node.peer === undefined) return;
		var foundedInArray = false;
		for(var i = 0; i < vm.nodesToCreate.length; i++) {
			if(vm.nodesToCreate[i].peer == node.peer && vm.nodesToCreate[i].name == node.name) {
				vm.nodesToCreate[i].peer = node.peer;
				vm.nodesToCreate[i].numberOfContainers = parseInt(vm.nodesToCreate[i].numberOfContainers) + parseInt(node.numberOfContainers);
				foundedInArray = true;
				break;
			}
		}

		if(!foundedInArray) {
			var copyNode = angular.copy(node);
			copyNode.parentNode = key;
			vm.nodesToCreate.push(copyNode);
		}

		nodeGroup.numberOfContainers = nodeGroup.numberOfContainers - node.numberOfContainers;
		if(nodeGroup.numberOfContainers > 0) {
			node.numberOfContainers = nodeGroup.numberOfContainers;
		} else {
			node.numberOfContainers = 0;
			nodeGroup.numberOfContainers = 0;
		}
	}

	function removeNode(key) {
		var parentKey = vm.nodesToCreate[key].parentNode;
		var numberOfContainers = parseInt(vm.createEnviromentInfo[parentKey].numberOfContainers) + parseInt(vm.nodesToCreate[key].numberOfContainers);
		vm.createEnviromentInfo[parentKey].numberOfContainers = numberOfContainers;
		vm.nodesToCreate.splice(key, 1);
	}

	function addBlueprint() {
		var finalBlueprint = vm.blueprintFrom;
		finalBlueprint.nodeGroups = vm.nodeList;
		if(finalBlueprint.currentNod !== undefined) {
			finalBlueprint.nodeGroups.push(finalBlueprint.currentNode);
		}
		delete finalBlueprint.currentNode;

		environmentService.createBlueprint(JSON.stringify(finalBlueprint)).success(function (data) {
			vm.blueprints.push(data);
		});

		vm.nodeList = [];
		vm.blueprintFrom = {};
	}

	function getTopology() {
		var topology = {};
		for(var i = 0; i < vm.nodesToCreate.length; i++) {
			vm.currentBlueprint.nodeGroups[vm.nodesToCreate[i].parentNode].numberOfContainers = vm.nodesToCreate[i].numberOfContainers;
			if(topology[vm.nodesToCreate[i].peer] === undefined) {
				topology[vm.nodesToCreate[i].peer] = [];
			}
			topology[vm.nodesToCreate[i].peer].push(vm.currentBlueprint.nodeGroups[vm.nodesToCreate[i].parentNode]);
		}
		return JSON.stringify(topology);
	}

	function buildBlueprint(){
		var topology = getTopology();
		var postData = 'name=' + vm.currentBlueprint.name;
		postData += '&topology=' + topology;
		postData += '&subnet=' + vm.subnetCIDR;
		postData += '&key=null';
		environmentService.buildBlueprint(encodeURI(postData)).success(function (data) {
			getEnvironments();
			SweetAlert.swal("Success!", "Your environment has been created.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", error.ERROR, "error");
		});
	}

	function growBlueprint() {
		var topology = getTopology();
		var postData = 'environmentId=' + vm.environmentToGrow + '&topology=' + topology;
		environmentService.growBlueprint(encodeURI(postData)).success(function (data) {
			console.log(data);
			SweetAlert.swal("Success!", "You successfully grow environment.", "success");
		}).error(function (error) {
			console.log(error);
			SweetAlert.swal("ERROR!", error.ERROR, "error");
		});
	}

	function destroyEnvironment(environmentId, key) {
		SweetAlert.swal({
				title: "Are you sure?",
				text: "Your will not be able to recover this Environment!",
				type: "warning",
				showCancelButton: true,
				confirmButtonColor: "#DD6B55",
				confirmButtonText: "Yes, destroy it!",
				cancelButtonText: "No, cancel plx!",
				closeOnConfirm: false,
				closeOnCancel: false,
				showLoaderOnConfirm: true
			},
			function (isConfirm) {
				if (isConfirm) {
					environmentService.destroyEnvironment(environmentId).success(function (data) {
						SweetAlert.swal("Destroyed!", "Your environment has been destroyed.", "success");
						vm.environments.splice(key, 1);
					}).error(function (data) {
						SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
					});
				} else {
					SweetAlert.swal("Cancelled", "Your environment is safe :)", "error");
				}
			});
	}

	function showContainersList(key) {
		vm.containers = vm.environments[key].containers;
		addPanel('envContainers');
	}

	function containerAction(key) {
		var action = 'start';
		if(vm.containers[key].state == 'RUNNING') {
			action = 'stop';
		}
		environmentService.switchContainer(vm.containers[key].id, action).success(function (data) {
			environmentService.getContainerStatus(vm.containers[key].id).success(function (data) {
				vm.containers[key].state = data.STATE;
			});				
		});		
	}

	function destroyContainer(containerId, key) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this Container!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#DD6B55",
			confirmButtonText: "Yes, destroy it!",
			cancelButtonText: "No, cancel plx!",
			closeOnConfirm: false,
			closeOnCancel: false,
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
			} else {
				SweetAlert.swal("Cancelled", "Your container is safe :)", "error");
			}
		});
	}

	function addNewNode() {
		if(vm.nodeStatus == 'Add to') {
			var tempNode = vm.blueprintFrom.currentNode;
			vm.blueprintFrom.currentNode = {};
			tempNode.containerPlacementStrategy.criteria = [];
			vm.nodeList.push(tempNode);
		} else {
			vm.blueprintFrom.currentNode = {};
			vm.nodeStatus = 'Add to';
		}
	}

	function setNodeData(key) {
		vm.nodeStatus = 'Update in';
		vm.blueprintFrom.currentNode = vm.nodeList[key];
	}

	function sshKey(key) {
		delete vm.enviromentSSHKey.key;
		vm.enviromentSSHKey.environmentKey = key;
		addPanel('envSshKey');
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

	function removeSshKey(){
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Delete environment SSH keys!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#DD6B55",
			confirmButtonText: "Yes, delete it!",
			cancelButtonText: "No, cancel plx!",
			closeOnConfirm: false,
			closeOnCancel: false,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {
				environmentService.removeSshKey(vm.environments[vm.enviromentSSHKey.environmentKey].id).success(function () {
					SweetAlert.swal("Destroyed!", "Your enviroment SSH keys has been deleted.", "success");
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your SSH keys is safe :). Error: " + data.ERROR, "error");
				});
			} else {
				SweetAlert.swal("Cancelled", "Your SSH keys is safe :)", "error");
			}
		});
	}	

	function getContainers() {
		var environment = vm.environments[vm.environmentQuota];
		vm.containersForQuota = environment.containers;
	}

	function contChanged(containerId) {
		vm.envQuota = {};
		environmentService.getEnvQuota(containerId).success(function (data) {
			vm.envQuota = data;
			vm.envQuota.containerId = containerId;
			/*for(var disk in vm.envQuota.disk) {
				console.log(vm.envQuota.disk[disk]);
			}*/
		});
	}

	function updateQuota() {
		console.log("update quota");
		var quotaPostData = 'cpu=' + vm.envQuota.cpu;
		quotaPostData += '&ram=' + vm.envQuota.ram;

		if(vm.envQuota.disk.HOME.diskQuotaValue > 0){
			quotaPostData += '&disk_home=' + vm.envQuota.disk.HOME.diskQuotaValue;
		} else {
			quotaPostData += '&disk_home=0';
		}

		if(vm.envQuota.disk.VAR.diskQuotaValue > 0){
			quotaPostData += '&disk_var=' + vm.envQuota.disk.VAR.diskQuotaValue;
		} else {
			quotaPostData += '&disk_var=0';
		}

		if(vm.envQuota.disk.ROOT_FS.diskQuotaValue > 0){
			quotaPostData += '&disk_root=' + vm.envQuota.disk.ROOT_FS.diskQuotaValue;
		} else {
			quotaPostData += '&disk_root=0';
		}

		if(vm.envQuota.disk.OPT.diskQuotaValue > 0){
			quotaPostData += '&disk_opt=' + vm.envQuota.disk.OPT.diskQuotaValue;
		} else {
			quotaPostData += '&disk_opt=0';
		}
		console.log(quotaPostData);

		environmentService.updateQuota(vm.envQuota.containerId, quotaPostData).success(function (data) {
			console.log(data);
		}).error(function (data) {
			console.log(data);
		});
	}	

	//// Implementation

	function addPanel(action) {
		jQuery('#resizable-pane').removeClass('fullWidthPane');
		if( action == 'createBlueprint' ) {
			jQuery('#build-blueprint-form').css('display', 'none');
			jQuery('#environment-containers-form').css('display', 'none');
			jQuery('#environment-sshkey-form').css('display', 'none');
			jQuery('#create-blueprint-form').css('display', 'block');
			jQuery('#environment-containers-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-sshkey-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#build-blueprint-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#create-blueprint-form').removeClass('bounceOutRight');
			jQuery('#create-blueprint-form').addClass('animated bounceInRight');
		}
		else if( action == 'buildBlueprint' ) {
			jQuery('#environment-containers-form').css('display', 'none');
			jQuery('#environment-sshkey-form').css('display', 'none');
			jQuery('#create-blueprint-form').css('display', 'none');
			jQuery('#build-blueprint-form').css('display', 'block');
			jQuery('#create-blueprint-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-sshkey-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-containers-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#build-blueprint-form').removeClass('bounceOutRight');
			jQuery('#build-blueprint-form').addClass('animated bounceInRight');
		}
		else if( action == 'envContainers' ) {
			jQuery('#create-blueprint-form').css('display', 'none');
			jQuery('#build-blueprint-form').css('display', 'none');
			jQuery('#environment-sshkey-form').css('display', 'none');
			jQuery('#environment-containers-form').css('display', 'block');
			jQuery('#create-blueprint-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-sshkey-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#build-blueprint-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-containers-form').removeClass('bounceOutRight');
			jQuery('#environment-containers-form').addClass('animated bounceInRight');
		}
		else if( action == 'envSshKey' ) {
			jQuery('#create-blueprint-form').css('display', 'none');
			jQuery('#build-blueprint-form').css('display', 'none');
			jQuery('#environment-containers-form').css('display', 'none');
			jQuery('#environment-sshkey-form').css('display', 'block');
			jQuery('#create-blueprint-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#build-blueprint-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-containers-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-sshkey-form').removeClass('bounceOutRight');
			jQuery('#environment-sshkey-form').addClass('animated bounceInRight');
		}
	}

	function closePanel(action) {
		jQuery('#resizable-pane').addClass('fullWidthPane');
		if( action == 'createBlueprint' ) {
			jQuery('#create-blueprint-form').addClass('bounceOutRight');
			jQuery('#create-blueprint-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#create-blueprint-form').css('display', 'none');
		}
		else if( action == 'buildBlueprint' ) {
			jQuery('#build-blueprint-form').addClass('bounceOutRight');
			jQuery('#build-blueprint-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#build-blueprint-form').css('display', 'none');
		}
		else if( action == 'envContainers' ) {
			jQuery('#environment-containers-form').addClass('bounceOutRight');
			jQuery('#environment-containers-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-containers-form').css('display', 'none');
		}
		else if( action == 'envSshKey' ) {
			jQuery('#environment-sshkey-form').addClass('bounceOutRight');
			jQuery('#environment-sshkey-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#environment-sshkey-form').css('display', 'none');
		}
	}

}
