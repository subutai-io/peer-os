'use strict';

angular.module('subutai.blueprints.controller', [])
	.controller('BlueprintsViewCtrl', BlueprintsViewCtrl)
	.controller('CreateBlueprintCtrl', CreateBlueprintCtrl);

BlueprintsViewCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'ngDialog', 'cfpLoadingBar'];
CreateBlueprintCtrl.$inject = ['$scope', 'environmentService', 'ngDialog', 'SweetAlert'];

function BlueprintsViewCtrl($scope, environmentService, SweetAlert, ngDialog, cfpLoadingBar) {

	var vm = this;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.blueprints = [];
	vm.hasEnvironments = false;

	vm.colors = quotaColors;


	getBlueprints();
	getEnvironments();

	// functions
	vm.createBlueprintFrom = createBlueprintFrom;
	vm.deleteBlueprint = deleteBlueprint;
	vm.removeNodeGroup = removeNodeGroup;


	function getBlueprints() {
		environmentService.getBlueprints()
			.success(function (data) {
				vm.blueprints = data;
			})
			.error(function (data) {
				VARS_MODAL_ERROR( SweetAlert, data );
			});
	}

	function getEnvironments() {
		environmentService.getEnvironments()
			.success(function (data) {
				for( var env in data )
				{
					if( data[env].status != STATUS_UNDER_MODIFICATION )
					{
						vm.hasEnvironments = true;
						break;
					}
				}
			})
			.error(function (data) {
				VARS_MODAL_ERROR( SweetAlert, data );
			});
	}

	function removeNodeGroup( blueprintId, nodeGroup ) {
		VARS_MODAL_CONFIRMATION( SweetAlert, 'Are you sure?', 'To remove Node Group from blueprint!',

			function (isConfirm) {
				if (isConfirm) {

					var blueprint = jQuery.grep( vm.blueprints, function( i ) {
						return i.id == blueprintId;
					});

					if( blueprint.length == 0 )
					{
						SweetAlert.swal(
							{
								title : 'Error!',
								text : 'Nodegroup name is incorrect!',
								timer: VARS_TOOLTIP_TIMEOUT,
								showConfirmButton: false
							}
						);
					}

					blueprint = blueprint[0];

					for ( var i = 0; i < blueprint.nodeGroups.length; i++ ) {
						if ( blueprint.nodeGroups[i].name == nodeGroup ) {
							blueprint.nodeGroups.splice( i, 1 );
							break;
						}
					}

					environmentService.saveBlueprint( JSON.stringify( blueprint ) )
						.success(function (data) {
							getBlueprints();

							SweetAlert.swal(
								{
									title : 'Deleted',
									text : 'Node group has been deleted!',
									timer: VARS_TOOLTIP_TIMEOUT,
									showConfirmButton: false
								}
							);
						}).error(function (data) {
							VARS_MODAL_ERROR( SweetAlert, data );
						});
				}
			}
		);
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
			controllerAs: 'cbl',
			data: dataToForm,
			preCloseCallback: function(value) {
				getBlueprints();
				getEnvironments();
			}
		});
	}

	function deleteBlueprint(blueprintId, key) {
		var currentBlueprint = vm.blueprints[key];
		VARS_MODAL_CONFIRMATION( SweetAlert, 'Are you sure?', 'To remove ' + currentBlueprint.name + ' blueprint!',
			function (isConfirm) {
				if (isConfirm) {
					environmentService.deleteBlueprint(blueprintId)
						.success(function (data) {
							getBlueprints();

							SweetAlert.swal(
								{
									title : 'Deleted',
									text : 'Blueprint has been deleted!',
									timer: VARS_TOOLTIP_TIMEOUT,
									showConfirmButton: false
								}
							);
						})
						.error(function (data) {
							VARS_MODAL_ERROR( SweetAlert, data );
						});
				}
			});
	}
}

function CreateBlueprintCtrl($scope, environmentService, ngDialog, SweetAlert) {

	var vm = this;

	vm.actionName = "Create";
	vm.colors = quotaColors;

	vm.blueprintFrom = {};
	vm.blueprintFrom.currentNode = getDefaultValues();
	vm.nodeList = [];
	vm.templates = [];
	vm.containersType = [];

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

	if($scope.ngDialogData !== undefined) {
		vm.actionName = "Edit";

		vm.blueprintFrom = $scope.ngDialogData;
		vm.nodeList = angular.copy(vm.blueprintFrom.nodeGroups);
		vm.blueprintFrom.nodeGroups = [];
		vm.blueprintFrom.currentNode = getDefaultValues();
	}

	vm.nodeStatus = 'Add to';
	vm.addBlueprintType = 'build';

	//functions
	vm.addBlueprint = addBlueprint;
	vm.addNewNode = addNewNode;
	vm.setNodeData = setNodeData;
	vm.removeNodeGroup = removeNodeGroup;
	vm.changeTemplate = changeTemplate;

	function addNewNode() {
		if(vm.nodeStatus == 'Add to') {
			var tempNode = vm.blueprintFrom.currentNode;

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


		vm.blueprintFrom.currentNode = angular.copy( vm.blueprintFrom.currentNode );
		vm.blueprintFrom.currentNode.name = "";
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

		environmentService.saveBlueprint(JSON.stringify(finalBlueprint))
			.success(function (data) {
				ngDialog.closeAll();
			})
			.error(function (data) {
				VARS_MODAL_ERROR( SweetAlert, data );
			});;

		vm.nodeList = [];
		vm.blueprintFrom = {};
	}

	function removeNodeGroup( name )
	{
		console.log("here");
		for( var i = 0; i < vm.nodeList.length; i++ )
		{
			if( vm.nodeList[i].name == name )
			{
				vm.nodeList.splice( i, 1 );
				break;
			}
		}
	}

	function changeTemplate(template) {
		if(template == 'hadoop') {
			vm.blueprintFrom.currentNode.sshGroupId = 1;
			vm.blueprintFrom.currentNode.hostsGroupId = 1;
		} else {
			vm.blueprintFrom.currentNode.sshGroupId = 0;
			vm.blueprintFrom.currentNode.hostsGroupId = 0;
		}
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
}
