'use strict';

angular.module('subutai.environment.controller', [])
	.controller('EnvironmentViewCtrl', EnvironmentViewCtrl)
	.directive('fileModel', fileModel);

EnvironmentViewCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'peerRegistrationService', 'SweetAlert', '$resource', '$compile', 'ngDialog', '$timeout', '$sce', '$stateParams', 'DTOptionsBuilder', 'DTColumnDefBuilder'];
fileModel.$inject = ['$parse'];

var fileUploder = {};

function EnvironmentViewCtrl($scope, $rootScope, environmentService, peerRegistrationService, SweetAlert, $resource, $compile, ngDialog, $timeout, $sce, $stateParams, DTOptionsBuilder, DTColumnDefBuilder) {

	var vm = this;
	var GRID_CELL_SIZE = 100;
	var GRID_SIZE = 100;

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
	vm.activeCloudTab = 'templates';

	vm.templateGrid = [];
	vm.cubeGrowth = 1;
	vm.environment2BuildName = 'Environment name';

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
	vm.initJointJs = initJointJs;
	vm.buildEnvironmentByJoint = buildEnvironmentByJoint;
	vm.sendToPending = sendToPending;
	vm.addSettingsToTemplate = addSettingsToTemplate;

	/*environmentService.getTemplates()
		.success(function (data) {
			vm.templates = data;
		})
		.error(function (data) {
			VARS_MODAL_ERROR( SweetAlert, 'Error on getting templates ' + data );
		});*/

	vm.templates = ['cassandra', 'mongo', 'zookeer', 'master', 'hadoop', 'spark', 'solr'];

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
			/*vm.environments = data;
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
			}*/
			vm.environments = [];
			for (var i = 0; i < data.length; ++i) {
				if (data[i].status !== "PENDING") {
					vm.environments.push(data[i]);
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
		SweetAlert.swal(
			{
				title : 'Environment',
				text : 'Creation has been started',
				timer: VARS_TOOLTIP_TIMEOUT,
				showConfirmButton: false
			}
		);		
		environmentService.startEnvironmentBuild (vm.newEnvID[0], encodeURIComponent(vm.newEnvID[1])).success(function (data) {
			SweetAlert.swal("Success!", "Your environment has been built successfully.", "success");
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

	var graph = new joint.dia.Graph;
	function initJointJs() {
		//custom shapes

		joint.shapes.tm = {};

		joint.shapes.tm.toolElement = joint.shapes.basic.Generic.extend({

			toolMarkup: [
				'<g class="element-tools">',
					'<g class="element-tool-remove">',
						'<circle fill="#F8FBFD" r="8" stroke="#dcdcdc"/>',
						'<polygon transform="scale(1.2) translate(-5, -5)" fill="#292F6C" points="8.4,2.4 7.6,1.6 5,4.3 2.4,1.6 1.6,2.4 4.3,5 1.6,7.6 2.4,8.4 5,5.7 7.6,8.4 8.4,7.6 5.7,5 "/>',
						'<title>Remove</title>',
					'</g>',
					'<g class="element-call-menu">',
						'<circle fill="#F8FBFD" r="8" stroke="#dcdcdc"/>',
						'<polygon transform="scale(1.2) translate(-5, -5)" fill="#292F6C" points="8.4,2.4 7.6,1.6 5,4.3 2.4,1.6 1.6,2.4 4.3,5 1.6,7.6 2.4,8.4 5,5.7 7.6,8.4 8.4,7.6 5.7,5 "/>',
						'<title>Menu</title>',
					'</g>',
				'</g>'
			].join(''),

			defaults: joint.util.deepSupplement({
				attrs: {
					text: { 'font-weight': 400, 'font-size': 'small', fill: 'black', 'text-anchor': 'middle', 'ref-x': .5, 'ref-y': .5, 'y-alignment': 'middle' },
					'g.element-call-menu': {'ref-x': 18, 'ref-y': 25}
				},
			}, joint.shapes.basic.Generic.prototype.defaults)

		});

		joint.shapes.tm.devElement = joint.shapes.tm.toolElement.extend({

			markup: [
				'<g class="rotatable">',
					'<g class="scalable">',
						'<rect class="b-border"/>',
					'</g>',
					'<title/>',
					'<image/>',
					'<rect class="b-magnet"/>',
				'</g>'
			].join(''),

			defaults: joint.util.deepSupplement({
				type: 'tm.devElement',
				size: { width: 70, height: 70 },
				attrs: {
					title: {text: 'Static Tooltip'},
					'rect.b-border': {fill: '#fff', stroke: '#dcdcdc', 'stroke-width': 1, width: 70, height: 70, rx: 50, ry: 50},
					'rect.b-magnet': {fill: '#04346E', width: 10, height: 10, rx: 2, ry: 2, magnet: true, transform: 'translate(30,53)'},
					image: {'ref-x': 9, 'ref-y': 9, ref: 'rect', width: 50, height: 50},
				}
			}, joint.shapes.tm.toolElement.prototype.defaults)
		});

		//custom view
		joint.shapes.tm.ToolElementView = joint.dia.ElementView.extend({
			initialize: function() {
				joint.dia.ElementView.prototype.initialize.apply(this, arguments);
			},
			render: function () {
				joint.dia.ElementView.prototype.render.apply(this, arguments);
				this.renderTools();
				this.update();
				return this;
			},
			renderTools: function () {
				var toolMarkup = this.model.toolMarkup || this.model.get('toolMarkup');
				if (toolMarkup) {
					var nodes = V(toolMarkup);
					V(this.el).append(nodes);
				}
				return this;
			},
			mouseover: function(evt, x, y) {
			},
			pointerclick: function (evt, x, y) {
				this._dx = x;
				this._dy = y;
				this._action = '';
				var className = evt.target.parentNode.getAttribute('class');
				switch (className) {
					case 'element-tool-remove':
						$('.js-add-dev-element[data-type=' + this.model.attributes.devType + ']')
							.removeClass('b-devops-menu__li-link_active');
						this.model.remove();
						$('.js-devops-item-info-block').hide();
						delete vm.templateGrid[Math.floor( x / GRID_CELL_SIZE )][ Math.floor( y / GRID_CELL_SIZE )];
						return;
						break;
					case 'element-call-menu':
						console.log(this.model);
						var elementPos = this.model.get('position');
						$('.js-dropen-menu').css({
							'left': (elementPos.x + 70) + 'px',
							'top': (elementPos.y + 83) + 'px',
							'display': 'block'
						});
						return;
						break;
					case 'rotatable':
						console.log(this.model);
						vm.currentTemplate = this.model;
						ngDialog.open({
							template: 'subutai-app/environment/partials/popups/templateSettings.html',
							scope: $scope
						});
						return;
						break;
					default:
				}
				joint.dia.CellView.prototype.pointerclick.apply(this, arguments);
			}
		});
		joint.shapes.tm.devElementView = joint.shapes.tm.ToolElementView;

		var paper = new joint.dia.Paper({
			el: $('#js-environment-creation'),
			width: '100%',
			height: '100%',
			model: graph,
			gridSize: 1
		});

		$('#js-build-devops').on('submit', function(){
			var allElements = graph.getCells();
			var connections = [];
			for(var i = 0; i < allElements.length; i++) {
				var currentElement = allElements[i];
				if(currentElement.attributes.type == 'link') {
					var source = graph.getCell(currentElement.attributes.source.id);
					var target = graph.getCell(currentElement.attributes.target.id);
					var connection = {};
					connection[source.attributes.devTypeId] = target.attributes.devTypeId;
					connections.push(connection);
				}
			}
			var buildObj = {};
			$('.js-build-field').each(function(){
				buildObj[$(this).attr('name')] = $(this).val();
			});
			buildObj.connections = connections;
			console.log(buildObj);
			return false;
		});

		var p0;
		paper.on('cell:pointerdown', function(cellView) {
			p0 = cellView.model.get('position');
		});

		paper.on('cell:pointerup',
			function(cellView, evt, x, y) {

				var pos = cellView.model.get('position');
				var p1 = { x: g.snapToGrid(pos.x, GRID_CELL_SIZE) + 20, y: g.snapToGrid(pos.y, GRID_CELL_SIZE) + 20 };

				var i = Math.floor( p1.x / GRID_CELL_SIZE );
				var j = Math.floor( p1.y / GRID_CELL_SIZE );

				if( vm.templateGrid[i] === undefined )
				{
					vm.templateGrid[i] = new Array();
				}

				if( vm.templateGrid[i][j] !== 1 )
				{
					vm.templateGrid[i][j] = 1;
					cellView.model.set('position', p1);
					vm.cubeGrowth = vm.cubeGrowth < ( i + 1 ) ? ( i + 1 ) : vm.cubeGrowth;
					vm.cubeGrowth = vm.cubeGrowth < ( j + 1 ) ? ( j + 1 ) : vm.cubeGrowth;

					i = Math.floor( p0.x / GRID_CELL_SIZE );
					j = Math.floor( p0.y / GRID_CELL_SIZE );

					delete vm.templateGrid[i][j];
				}
				else
					cellView.model.set('position', p0);
			}
		);

		$('.js-scrollbar').perfectScrollbar();
		var containerCounter = 1;
		$('.b-tools-menu').on('click', '.js-add-dev-element', function(){
			var pos = findEmptyCubePostion();
			var img = $(this).find('img');
			var devElement = new joint.shapes.tm.devElement({
				position: { x: (GRID_CELL_SIZE * pos.x) + 20, y: (GRID_CELL_SIZE * pos.y) + 20 },
				//devType: $(this).data('type'),
				templateName: $(this).data('template'),
				quotaSize: 'SMALL',
				containerName: vm.environment2BuildName + ' ' + (containerCounter++).toString(),
				attrs: {
					image: { 'xlink:href': img.attr('src') },
					title: {text: $(this).data('template')}
				}
			});
			graph.addCell(devElement);
			return false;
		});

		function findEmptyCubePostion() {
			for( var j = 0; j < vm.cubeGrowth; j++ ) {
				for( var i = 0; i < vm.cubeGrowth; i++ ) {
					if( vm.templateGrid[i] === undefined ) {
						vm.templateGrid[i] = new Array();
						vm.templateGrid[i][j] = 1;

						return {x:i, y:j};
					}

					if( vm.templateGrid[i][j] !== 1 ) {
						vm.templateGrid[i][j] = 1;
						return {x:i, y:j};
					}
				}
			}

			vm.templateGrid[vm.cubeGrowth] = new Array();
			vm.templateGrid[vm.cubeGrowth][0] = 1;
			vm.cubeGrowth++;
			return { x : vm.cubeGrowth - 1, y : 0 };
		}

		paper.$el.on('mousewheel DOMMouseScroll', onMouseWheel);

		function onMouseWheel(e) {

			e.preventDefault();
			e = e.originalEvent;

			var delta = Math.max(-1, Math.min(1, (e.wheelDelta || -e.detail))) / 50;
			var offsetX = (e.offsetX || e.clientX - $(this).offset().left); // offsetX is not defined in FF
			var offsetY = (e.offsetY || e.clientY - $(this).offset().top); // offsetY is not defined in FF
			var p = offsetToLocalPoint(offsetX, offsetY);
			var newScale = V(paper.viewport).scale().sx + delta; // the current paper scale changed by delta

			if (newScale > 0.4 && newScale < 2) {
				paper.setOrigin(0, 0); // reset the previous viewport translation
				paper.scale(newScale, newScale, p.x, p.y);
			}
		}

		function offsetToLocalPoint(x, y) {
			var svgPoint = paper.svg.createSVGPoint();
			svgPoint.x = x;
			svgPoint.y = y;
			// Transform point into the viewport coordinate system.
			var pointTransformed = svgPoint.matrixTransform(paper.viewport.getCTM().inverse());
			return pointTransformed;
		}
	}

	vm.buildStep = 'confirm';
	function buildEnvironmentByJoint() {
		var allElements = graph.getCells();
		vm.env2Build = {};
		vm.containers2Build = [];
		vm.buildStep = 'confirm';
		console.log(allElements);
		for(var i = 0; i < allElements.length; i++) {
			var currentElement = allElements[i];
			var currentTemplateName = allElements[i].get('templateName');
			var container2Build = {
				"size": currentElement.get('quotaSize'),
				"templateName": currentTemplateName,
				"name": currentElement.get('containerName'),
				"position": currentElement.get('position')
			};
			if(vm.env2Build[currentTemplateName] === undefined) {
				vm.env2Build[currentTemplateName] = 1;
			} else {
				vm.env2Build[currentTemplateName]++;
			}
			vm.containers2Build.push(container2Build);
		}
		console.log(vm.containers2Build);
		ngDialog.open({
			template: 'subutai-app/environment/partials/popups/environment-build-info.html',
			scope: $scope,
			className: 'b-build-environment-info'
		});
	}

	function sendToPending() {
		LOADING_SCREEN();
		environmentService.startEnvironmentAutoBuild(vm.environment2BuildName, JSON.stringify(vm.containers2Build))
			.success(function(data){
				console.log(data);
				vm.newEnvID = data;
				vm.buildStep = 'pgpKey';
				LOADING_SCREEN('none');
			}).error(function(error){
				if(error.ERROR === undefined) {
					VARS_MODAL_ERROR( SweetAlert, 'Error: ' + error );
				} else {
					VARS_MODAL_ERROR( SweetAlert, 'Error: ' + error.ERROR );
				}
				LOADING_SCREEN('none');
			});
	}

	function addSettingsToTemplate(settings) {
		vm.currentTemplate.set('quotaSize', settings.quotaSize);
		vm.currentTemplate.set('containerName', settings.containerName);
		ngDialog.closeAll();
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

