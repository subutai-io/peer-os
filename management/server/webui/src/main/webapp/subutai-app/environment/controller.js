'use strict';

angular.module('subutai.environment.controller', [])
	.controller('EnvironmentViewCtrl', EnvironmentViewCtrl)
	.directive('fileModel', fileModel);

EnvironmentViewCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'peerRegistrationService', 'SweetAlert', '$resource', '$compile', 'ngDialog', '$timeout', '$sce', '$stateParams', 'DTOptionsBuilder', 'DTColumnDefBuilder'];
fileModel.$inject = ['$parse'];

var fileUploder = {};

function EnvironmentViewCtrl($scope, $rootScope, environmentService, peerRegistrationService, SweetAlert, $resource, $compile, ngDialog, $timeout, $sce, $stateParams, DTOptionsBuilder, DTColumnDefBuilder) {

	var vm = this;
	vm.activeTab = $stateParams.activeTab;
	if (vm.activeTab !== "pending") {
		vm.activeTab = "installed";
	}
	vm.currentEnvironment = {};
	vm.signedMessage = "";
	vm.buildEnvironment = buildEnvironment;

	vm.environments = [];
	vm.domainStrategies = [];
	vm.strategies = [];
	vm.sshKeyForEnvironment = '';
	vm.environmentForDomain = '';
	vm.currentDomain = {};
	vm.selectedPeers = [];
	vm.installed = false;
	vm.pending = false;
	vm.isDataValid = isDataValid;

	vm.peerIds = [];
	vm.advancedEnv = {};
	vm.advancedEnv.currentNode = getDefaultValues();
	vm.advancedModeEnabled = false;
	vm.nodeStatus = 'Add to';
	vm.nodeList = [];
	vm.colors = quotaColors;
	vm.templates = [];
	vm.containersType = [];

	vm.sshKeys = [];

	// functions
	vm.showEnvironmentForm = showEnvironmentForm;
	vm.destroyEnvironment = destroyEnvironment;
	vm.startEnvironmentBuild = startEnvironmentBuild;
	vm.sshKey = sshKey;
	vm.addSshKey = addSshKey;
	vm.removeSshKey = removeSshKey;
	//vm.getEnvironments = getEnvironments;
	vm.showContainersList = showContainersList;
	vm.destroyContainer = destroyContainer;
	vm.setSSHKey = setSSHKey;
	vm.showSSHKeyForm = showSSHKeyForm;
	vm.showSSHKeysPopup = showSSHKeysPopup;
	vm.showDomainForm = showDomainForm;
	vm.setDomain = setDomain;
	vm.removeDomain = removeDomain;
	vm.togglePeer = togglePeer;
	vm.setupStrategyRequisites = setupStrategyRequisites;

	vm.addNewNode = addNewNode;
	vm.removeNodeGroup = removeNodeGroup;
	vm.setNodeData = setNodeData;
	vm.setupAdvancedEnvironment = setupAdvancedEnvironment;

	environmentService.getTemplates()
		.success(function (data) {
			vm.templates = data;
		})
		.error(function (data) {
			VARS_MODAL_ERROR( SweetAlert, 'Error on getting templates ' + data );
		});

	environmentService.getContainersType()
		.success(function (data) {
			vm.containersType = data;
		})
		.error(function (data) {
			VARS_MODAL_ERROR( SweetAlert, data );
		});

	vm.containersTotal = [];
	function loadEnvironments() {
		vm.containersTotal = [];
		environmentService.getEnvironments().success (function (data) {
			vm.environments = data;
			for (var i = 0; i < vm.environments.length; ++i) {
				if (vm.environments[i].status !== "PENDING") {
					vm.installed = true;
					if (vm.pending) {
						break;
					}
				}
				else {
					vm.pending = true;
					if (vm.installed) {
						break;
					}
				}
			}
		});
	}
	loadEnvironments();

	environmentService.getStrategies().success(function (data) {
		vm.strategies = data;
	});

	environmentService.getDomainStrategies().success(function (data) {
		vm.domainStrategies = data;
	});

	environmentService.getPeers().success(function (data) {
		vm.peerIds = data;
	});

	peerRegistrationService.getRequestedPeers().success(function (peers) {
		peers.unshift({peerInfo: {id: 'local'}});
		vm.peers = peers;
	});

	//installed environment table options
	vm.dtOptionsInstallTable = DTOptionsBuilder
		.newOptions()
		.withOption('order', [[ 1, "asc" ]])
		.withOption('stateSave', true)
		.withPaginationType('full_numbers');
	vm.dtColumnDefsInstallTable = [
		DTColumnDefBuilder.newColumnDef(0).notSortable(),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2).notSortable(),
		DTColumnDefBuilder.newColumnDef(3).notSortable(),
		DTColumnDefBuilder.newColumnDef(4).notSortable(),
		DTColumnDefBuilder.newColumnDef(5).notSortable(),
	];

	//pending environment table options
	vm.dtOptionsPendingTable = DTOptionsBuilder
		.newOptions()
		.withOption('order', [[ 1, "asc" ]])
		.withOption('stateSave', true)
		.withPaginationType('full_numbers');
	vm.dtColumnDefsPendingTable = [
		DTColumnDefBuilder.newColumnDef(0).notSortable(),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2).notSortable()
	];

	vm.listOfUsers = [];
	vm.shareEnvironmentWindow = shareEnvironmentWindow;
	vm.toggleSelection = toggleSelection;
	vm.shareEnvironment = shareEnvironment;
	vm.users2Add = [];
	vm.addUser2Stack = addUser2Stack;
	vm.removeUserFromStack = removeUserFromStack;
	vm.containersTags = containersTags;
	vm.installedContainers = null;
	function addUser2Stack(user) {
		vm.users2Add.push(angular.copy(user));
		for (var i = 0; i < vm.listOfUsers.length; ++i) {
			if (vm.listOfUsers[i].fullName === user.fullName) {
				vm.listOfUsers.splice (i, 1);
				break;
			}
		}
	}


	function removeUserFromStack(key) {
		vm.listOfUsers.push (vm.users2Add[key]);
		vm.users2Add.splice(key, 1);
	}

	vm.currentUser = {};
	environmentService.getCurrentUser().success (function (data) {
		vm.currentUser = data;
	});
	function shareEnvironmentWindow (environment) {
		vm.listOfUsers = [];
		vm.checkedUsers = [];
		environmentService.getUsers().success(function (data) {
			for (var i = 0; i < data.length; ++i) {
				if (data[i].id !== vm.currentUser.id) {
					vm.listOfUsers.push (data[i]);
				}
			}
			for (var i = 0; i < vm.listOfUsers.length; ++i) {
				vm.listOfUsers[i].read = true;
				vm.listOfUsers[i].write = true;
				vm.listOfUsers[i].update = true;
				vm.listOfUsers[i].delete = true;
			}
			environmentService.getShared(environment.id).success(function (data2) {
				vm.users2Add = data2;
				for (var i = 0; i < vm.users2Add.length; ++i) {
					if (vm.users2Add[i].id === vm.currentUser.id) {
						vm.users2Add.splice (i, 1);
						--i;
						continue;
					}
					for (var j = 0; j < vm.listOfUsers.length; ++j) {
						if (vm.listOfUsers[j].id === vm.users2Add[i].id) {
							vm.users2Add[i].fullName = vm.listOfUsers[j].fullName;
							vm.listOfUsers.splice (j, 1);
							break;
						}
					}
				}
				vm.currentEnvironment = environment;
				ngDialog.open ({
					template: "subutai-app/environment/partials/popups/shareEnv.html",
					scope: $scope
				});
			});
		});
	}

	function toggleSelection (user) {
		for (var i = 0; i < vm.checkedUsers.length; ++i) {
			if (vm.checkedUsers[i].id === user.id) {
				vm.checkedUsers.splice (i, 1);
				return;
			}
		}
		vm.checkedUsers.push (user);
	}

	function shareEnvironment() {
		var arr = [];
		for (var i = 0; i < vm.users2Add.length; ++i) {
			arr.push ({
				id: vm.users2Add[i].id,
				read: vm.users2Add[i].read,
				write: vm.users2Add[i].write,
				update: vm.users2Add[i].update,
				delete: vm.users2Add[i].delete
			});
		}
		environmentService.share (JSON.stringify (arr), vm.currentEnvironment.id).success(function (data) {
			SweetAlert.swal("Success!", "Your environment was successfully shared.", "success");
			loadEnvironments();
			ngDialog.closeAll();
		}).error(function (data) {
			SweetAlert.swal("ERROR!", "Your container is safe :). Error: " + data.ERROR, "error");
		});
	}

	function setupStrategyRequisites(environment) {
		LOADING_SCREEN();
		ngDialog.closeAll();
		environmentService.setupStrategyRequisites(
			environment.name,
			environment.strategy,
			environment.sshGroupId,
			environment.hostGroupId,
			vm.selectedPeers
		).success(function () {
			vm.selectedPeers = [];
			SweetAlert.swal("Success!!", "Your environment was successfully configured, please approve it.", "success");
			vm.activeTab = 'pending';
			LOADING_SCREEN("none");
		}).error(function (data) {
			ngDialog.closeAll();
			SweetAlert.swal("ERROR!", "Your container is safe :). Error: " + data.ERROR, "error");
			LOADING_SCREEN("none");
		});
	}

	function isDataValid() {
		return vm.selectedPeers.length > 0;
	}

	function actionSwitch (data, type, full, meta) {
		if (typeof (data.revoke) === "boolean") {
			return '<div class = "toggle"><input type = "checkbox" class="check" ng-click="environmentViewCtrl.revoke(\'' + data.id + '\')" ng-checked=\'' + data.revoke + '\'><div class = "toggle-bg"></div><b class = "b switch"></b></div>'
		}
		else {
			return "";
		}
	}

	vm.revoke = revoke;

	function revoke (environmentId) {
		environmentService.revoke (environmentId).success (function (data) {
			loadEnvironments();
		});
	}

	var refreshTable;
	var reloadTableData = function() {
		refreshTable = $timeout(function myFunction() {
			loadEnvironments();
			refreshTable = $timeout(reloadTableData, 30000);
		}, 30000);
	};
	reloadTableData();

	$rootScope.$on('$stateChangeStart',	function(event, toState, toParams, fromState, fromParams){
		console.log('cancel');
		$timeout.cancel(refreshTable);
	});


	/*	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function statusHTML(environmentStatus, type, full, meta) {
		return '<div class="b-status-icon b-status-icon_' + environmentStatus + '" tooltips tooltip-template="' + environmentStatus + '" tooltip-side="right"></div>';
	}

	function environmentNameTooltip(data, type, full, meta) {
		vm.users[data.id] = data;
		return "<span tooltips tooltip-template='<span class=\"b-nowrap\">ID: <b>" + data.id + "</b></span>'>" + data.name + "</span>";
	}

	function sshKeyLinks(data, type, full, meta) {
		var addSshKeyLink = '<a href ng-click="environmentViewCtrl.showSSHKeyForm(\'' + data.id + '\')">Add</a>';
		var removeSshKeyLink = '<a href ng-click="environmentViewCtrl.removeSshKey(\'' + data.id + '\')">Remove</a>';
		return addSshKeyLink + '/' + removeSshKeyLink;
	}

	function domainsTag(data, type, full, meta) {
		return '<button class="b-btn b-btn_grey" ng-click="environmentViewCtrl.showDomainForm(\'' + data.id + '\')">Configure</button>';
		//return '<span class="b-tags b-tags_grey" ng-click="environmentViewCtrl.removeDomain(\'' + data.id + '\')">Add <i class="fa fa-plus"></i></span>';
	}

	function actionStartEnvironmentBuild(data, type, full, meta) {
		return '<a href="" class="b-icon b-icon_build" ng-click="environmentViewCtrl.startEnvironmentBuild(\'' + data.id + '\')"></a>';
	}*/

	function containersTags (data) {
		var containersTotal = {};
		for(var i = 0; i < data.containers.length; i++) {
			if(containersTotal[data.containers[i].templateName] === undefined) {
				containersTotal[data.containers[i].templateName] = {};
			}

			if(containersTotal[data.containers[i].templateName][data.containers[i].type] === undefined) {
				containersTotal[data.containers[i].templateName][data.containers[i].type] = 0;
			}

			if(data.containers[i].state != 'RUNNING') {
				if(containersTotal[data.containers[i].templateName]['INACTIVE'] === undefined) {
					containersTotal[data.containers[i].templateName]['INACTIVE'] = 0;
				}
				containersTotal[data.containers[i].templateName]['INACTIVE'] += 1;
			} else {
				containersTotal[data.containers[i].templateName][data.containers[i].type] += 1;
			}
		}

		var containersHTML = '';
		for(var template in containersTotal) {
			for (var type in containersTotal[template]){
				if(containersTotal[template][type] > 0) {
					if(type != 'INACTIVE') {
						var tooltipContent = '<div class="b-nowrap">Quota: <div class="b-quota-type-round b-quota-type-round_' + quotaColors[type] + '"></div> <b>' + type + '</b></div><span class="b-nowrap">State: <b>RUNNING</b></span>';
					} else {
						var tooltipContent = 'State: <b>INACTIVE</b>';
					}
					containersTotal[template].color = quotaColors[type];
					containersTotal[template].counts = containersTotal[template][type];
					containersTotal[template].type = type;
					containersTotal[template].tooltip = tooltipContent;
					containersTotal[template].dataID = data.id;
				}
			}
		}
		vm.installedContainers = containersTotal;
	}

/*	function actionDelete(data, type, full, meta) {
		return '<a href="" class="b-icon b-icon_remove" ng-click="environmentViewCtrl.destroyEnvironment(\'' + data.id + '\')"></a>';
	}*/

	function destroyContainer(containerId) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "You will not be able to recover this Container!",
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
					loadEnvironments();
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your container is safe :). Error: " + data.ERROR, "error");
				});
			}
		});
	}

    function startEnvironmentBuild (environment) {
    	vm.currentEnvironment = environment;
		ngDialog.open ({
			template: "subutai-app/environment/partials/popups/decryptMsg.html",
			scope: $scope,
			className: 'environmentDialog'
		});
    }

	function showEnvironmentForm() {
		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/createEnvironment.html',
			scope: $scope
		})
	}

	function togglePeer(peerId) {
		vm.selectedPeers.indexOf(peerId) === -1 ?
				vm.selectedPeers.push(peerId) :
				vm.selectedPeers.splice(vm.selectedPeers.indexOf(peerId), 1);
	}

	function buildEnvironment() {
		ngDialog.closeAll();
		vm.activeTab = "installed";
		environmentService.startEnvironmentBuild (vm.currentEnvironment.id, encodeURIComponent(vm.currentEnvironment.relationDeclaration)).success(function (data) {
			SweetAlert.swal("Success!", "Your environment has started building.", "success");
			loadEnvironments();
		}).error(function (data) {
			SweetAlert.swal("ERROR!", "Environment build error. Error: " + data.ERROR, "error");
		});
	}


	function destroyEnvironment(environmentId) {
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
						SweetAlert.swal("Destroyed!", "Your environment has been destroyed.", "success");
						loadEnvironments();
					}).error(function (data) {
						SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
					});
					loadEnvironments();
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
		environmentService.setSshKey(vm.enviromentSSHKey.key, enviroment.id).success(function (data) {
			SweetAlert.swal("Success!", "You have successfully added SSH key for " + enviroment.id + " environment!", "success");
		}).error(function (error) {
			SweetAlert.swal("Cancelled", "Error: " + error.ERROR, "error");
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
					SweetAlert.swal("Destroyed!", "Your SSH keys has been deleted.", "success");
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

	function showSSHKeyForm(environmentId) {
		vm.sshKeyForEnvironment = environmentId;
		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/sshKeyForm.html',
			scope: $scope
		});
	}

	function showSSHKeysPopup(environmentId) {

		environmentService.getSshKey(environmentId).success( function (data) {
			console.log( data );
		})
		.error( function(data) {
			SweetAlert.swal("Cancelled", "Error: " + data.ERROR, "error");
			ngDialog.closeAll();
			LOADING_SCREEN('none');
		});


		//ngDialog.open({
		//	template: 'subutai-app/environment/partials/popups/sshKeysPopup.html',
		//	scope: $scope
		//});
	}

	function showDomainForm(environmentId) {
		vm.environmentForDomain = environmentId;
		vm.currentDomain = {};
		LOADING_SCREEN();
		environmentService.getDomain(environmentId).success(function (data) {
			vm.currentDomain = data;
			ngDialog.open({
				template: 'subutai-app/environment/partials/popups/domainForm.html',
				scope: $scope
			});
			LOADING_SCREEN('none');
		});
	}

	function setDomain(domain) {
		var file = fileUploder;
		LOADING_SCREEN();
		environmentService.setDomain(domain, vm.environmentForDomain, file).success(function (data) {
			SweetAlert.swal("Success!", "You have successfully added domain for " + vm.environmentForDomain + " environment!", "success");
			ngDialog.closeAll();
			LOADING_SCREEN('none');
		}).error(function (data) {
			SweetAlert.swal("Cancelled", "Error: " + data.ERROR, "error");
			ngDialog.closeAll();
			LOADING_SCREEN('none');
		});
	}

	function removeDomain(environmentId) {
		ngDialog.closeAll();
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Delete environment domain!",
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
				environmentService.removeDomain(environmentId).success(function (data) {
					SweetAlert.swal("Deleted!", "Your domain has been deleted.", "success");
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your domain is safe. Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function setSSHKey(sshKey) {
		console.log(sshKey);
		if(sshKey === undefined || sshKey.length <= 0 || sshKey === null) return;
		environmentService.setSshKey(sshKey, vm.sshKeyForEnvironment).success(function (data) {
			SweetAlert.swal("Success!", "You have successfully added SSH key for " + vm.sshKeyForEnvironment + " environment!", "success");
			ngDialog.closeAll();
		}).error(function (data) {
			SweetAlert.swal("Cancelled", "Error: " + data.ERROR, "error");
			ngDialog.closeAll();
		});
	}


	vm.setHtml = setHtml;
	function setHtml (html) {
		return $sce.trustAsHtml(html.toString());
	};


	function addNewNode() {
		if(vm.nodeStatus == 'Add to') {
			var tempNode = vm.advancedEnv.currentNode;

			if(tempNode === undefined) return;
			if(tempNode.name === undefined || tempNode.name.length < 1) return;
			if(tempNode.numberOfContainers === undefined || tempNode.numberOfContainers < 1) return;
			if(tempNode.sshGroupId === undefined) return;
			if(tempNode.hostsGroupId === undefined) return;

			if( jQuery.grep( vm.nodeList, function( i ) {
					return tempNode.name == i.name;
				}).length != 0
			) return;

			vm.nodeList.push(tempNode);
		} else {
			vm.nodeStatus = 'Add to';
		}


		vm.advancedEnv.currentNode = angular.copy( vm.advancedEnv.currentNode );
		vm.advancedEnv.currentNode.name = "";
	}

	function setNodeData(key) {
		vm.nodeStatus = 'Update in';
		vm.advancedEnv.currentNode = vm.nodeList[key];
	}

	function removeNodeGroup(key)
	{
		vm.nodeList.splice(key, 1);
	}

	function getDefaultValues() {
		var defaultVal = {
			'templateName': 'master',
			'numberOfContainers': 2,
			'sshGroupId': 0,
			'hostsGroupId': 0,
			'type': 'TINY'
		};
		return defaultVal;
	}

	function setupAdvancedEnvironment() {
		if(vm.advancedEnv.name === undefined) return;
		if(vm.nodeList === undefined || vm.nodeList.length == 0) return;

		var finalEnvironment = vm.advancedEnv;
		finalEnvironment.nodeGroups = vm.nodeList;
		if(finalEnvironment.currentNod !== undefined) {
			finalEnvironment.nodeGroups.push(finalEnvironment.currentNode);
		}
		delete finalEnvironment.currentNode;

		var cloneContainers = {};

		for( var i = 0; i < finalEnvironment.nodeGroups.length; i++ )
		{
			var node = finalEnvironment.nodeGroups[i];
			for( var j = 0; j < node.numberOfContainers; j++ )
			{
				if( j < 0 ) break;

				if( cloneContainers[node.peerId] === undefined )
				{
					cloneContainers[node.peerId] = [];
				}

				cloneContainers[node.peerId].push(node);
			}
		}

		console.log(cloneContainers);
		LOADING_SCREEN();
		ngDialog.closeAll();
		vm.activeTab = 'pending';
		environmentService.setupAdvancedEnvironment(finalEnvironment.name, cloneContainers)
			.success(function(data){
				console.log(data);
				loadEnvironments();
				LOADING_SCREEN('none');
			}).error(function(error){
				console.log(error);
				LOADING_SCREEN('none');
			});

		vm.nodeList = [];
		vm.advancedEnv = {};
		vm.advancedEnv.currentNode = getDefaultValues();
	}
}

function fileModel($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function(){
				document.getElementById("js-uploadFile").value = element[0].files[0].name;
				scope.$apply(function(){
					modelSetter(scope, element[0].files[0]);
					fileUploder = element[0].files[0];
				});
			});
		}
	};
}

