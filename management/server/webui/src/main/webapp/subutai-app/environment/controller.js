'use strict';

angular.module('subutai.environment.controller', [])
	.controller('EnvironmentViewCtrl', EnvironmentViewCtrl)
	.directive('fileModel', fileModel);

EnvironmentViewCtrl.$inject = ['$scope', 'environmentService', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'ngDialog', '$timeout'];
fileModel.$inject = ['$parse'];

var fileUploder = {};

function EnvironmentViewCtrl($scope, environmentService, SweetAlert, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, ngDialog, $timeout) {

	var vm = this;
	vm.environments = [];
	vm.domainStrategies = [];
	vm.sshKeyForEnvironment = '';
	vm.environmentForDomain = '';

	// functions
	vm.destroyEnvironment = destroyEnvironment;
	vm.sshKey = sshKey;
	vm.addSshKey = addSshKey;
	vm.removeSshKey = removeSshKey;
	vm.getEnvironments = getEnvironments;
	vm.showContainersList = showContainersList;
	vm.destroyContainer = destroyContainer;
	vm.setSSHKey = setSSHKey;
	vm.showSSHKeyForm = showSSHKeyForm;
	vm.showDomainForm = showDomainForm;
	vm.setDomain = setDomain;
	vm.removeDomain = removeDomain;

	function getEnvironments() {
		environmentService.getEnvironments().success(function (data) {
			vm.environments = data;
		});
	}

	environmentService.getDomainStrategies().success(function (data) {
		vm.domainStrategies = data;
	});

	getEnvironments();

	vm.dtInstance = {};
	vm.users = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( environmentService.getServerUrl() ) .query().$promise;
		}).withPaginationType('full_numbers')
		.withOption('createdRow', createdRow)
		.withOption('order', [[ 1, "asc" ]])
		//.withDisplayLength(2)
		.withOption('stateSave', true);

	vm.dtColumns = [
		//DTColumnBuilder.newColumn('id').withTitle('ID'),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(statusHTML),
		DTColumnBuilder.newColumn('name').withTitle('Environment name'),
		DTColumnBuilder.newColumn(null).withTitle('Key SSH').renderWith(sshKeyLinks),
		DTColumnBuilder.newColumn(null).withTitle('Domains').renderWith(domainsTag),
		DTColumnBuilder.newColumn(null).withTitle('').renderWith(containersTags),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionDelete)
	];

	/*function reloadTableData() {
		vm.refreshTable = $timeout(function myFunction() {
			vm.dtInstance.reloadData(null, false);
			vm.refreshTable = $timeout(reloadTableData, 10000);
		}, 10000);
	};
	reloadTableData();*/

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function statusHTML(data, type, full, meta) {
		vm.users[data.id] = data;
		return '<div class="b-status-icon b-status-icon_' + data.status + '" tooltips tooltip-title="' + data.status + '"></div>';
	}

	function sshKeyLinks(data, type, full, meta) {
		var addSshKeyLink = '<a href ng-click="environmentViewCtrl.showSSHKeyForm(\'' + data.id + '\')">Add</a>';
		var removeSshKeyLink = '<a href ng-click="environmentViewCtrl.removeSshKey(\'' + data.id + '\')">Remove</a>';
		return addSshKeyLink + '/' + removeSshKeyLink;
	}

	function domainsTag(data, type, full, meta) {
		return '<span class="b-tags b-tags_grey" ng-click="environmentViewCtrl.showDomainForm(\'' + data.id + '\')">Add <i class="fa fa-plus"></i></span>';
		//return '<span class="b-tags b-tags_grey" ng-click="environmentViewCtrl.removeDomain(\'' + data.id + '\')">Add <i class="fa fa-plus"></i></span>';
	}

	function containersTags(data, type, full, meta) {

		var containersTotal = [];
		for(var i = 0; i < data.containers.length; i++) {
			if(containersTotal[data.containers[i].templateName] === undefined) {
				containersTotal[data.containers[i].templateName] = [];
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
						var tooltipContent = 'Quota: <div class="b-quota-type-round b-quota-type-round_' + quotaColors[type] + '"></div> <b>' + type + '</b><br>State: <b>RUNNING</b>';
					} else {
						var tooltipContent = 'State: <b>INACTIVE</b>';
					}
					containersHTML += '<a ui-sref="containers({environmentId:\'' + data.id + '\'})" ' 
						+ ' class="b-tags b-tags_' + quotaColors[type] + '" ' 
						+ 'tooltips tooltip-content=\'' + tooltipContent + '\' tooltip-hide-trigger="mouseleave click" '
						+ '>' 
						+ template + ': ' + containersTotal[template][type] 
					+ '</a>';
				}
			}
		}

		/*var containersHTML = '';
		for(var i = 0; i < data.containers.length; i++) {
			var tooltipContent = 'IP: <b>' + data.containers[i].ip + '</b><br> Quota: <div class="b-quota-type-round b-quota-type-round_' + quotaColors[data.containers[i].type] + '"></div> <b>' + data.containers[i].type + '</b><br>State: <b>' + data.containers[i].state + '</b>';
			containersHTML += '<span ' 
				+ ' class="b-tags b-tags_' + quotaColors[data.containers[i].type] + '" ' 
				+ 'tooltips tooltip-content=\'' + tooltipContent + '\' '
				+ '>' 
				+ '<a ui-sref="containers({environmentId:\'' + data.id + '\'})">' + data.containers[i].templateName + '</a>' 
				+ ' <a href ng-click="environmentViewCtrl.destroyContainer(\'' + data.containers[i].id + '\')"><i class="fa fa-times"></i></a>' 
			+ '</span>';
		}*/
		return containersHTML;
	}

	function actionDelete(data, type, full, meta) {
		return '<a href="" class="b-icon b-icon_remove" ng-click="environmentViewCtrl.destroyEnvironment(\'' + data.id + '\')"></a>';
	}

	function destroyContainer(containerId) {
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
					vm.dtInstance.reloadData(null, false);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function destroyEnvironment(environmentId) {
		SweetAlert.swal({
				title: "Are you sure?",
				text: "Your will not be able to recover this Environment!",
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
					SweetAlert.swal("Delete!", "Your environment start deleting!", "success");
					environmentService.destroyEnvironment(environmentId).success(function (data) {
						SweetAlert.swal("Destroyed!", "Your environment has been destroyed.", "success");
						vm.dtInstance.reloadData(null, false);
					}).error(function (data) {
						SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
					});
					vm.dtInstance.reloadData(null, false);
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
			SweetAlert.swal("Success!", "You successfully add SSH key for " + enviroment.id + " environment!", "success");
		}).error(function (data) {
			SweetAlert.swal("Cancelled", "Error: " + data.ERROR, "error");
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
					SweetAlert.swal("Destroyed!", "Your enviroment SSH keys has been deleted.", "success");
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
			template: 'subutai-app/environment/partials/sshKeyForm.html',
			scope: $scope
		});
	}

	function showDomainForm(environmentId) {
		vm.environmentForDomain = environmentId;
		ngDialog.open({
			template: 'subutai-app/environment/partials/domainForm.html',
			scope: $scope
		});
	}

	function setDomain(domain) {
		var file = fileUploder;
		environmentService.setDomain(domain, vm.environmentForDomain, file).success(function (data) {
			SweetAlert.swal("Success!", "You successfully add domain for " + vm.environmentForDomain + " environment!", "success");
			//ngDialog.closeAll();
			console.log(data);
		}).error(function (data) {
			SweetAlert.swal("Cancelled", "Error: " + data.ERROR, "error");
			//ngDialog.closeAll();
		});
	}

	function removeDomain(environmentId) {
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
					SweetAlert.swal("Deleted!", "Your enviroment domain has been deleted.", "success");
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
			SweetAlert.swal("Success!", "You successfully add SSH key for " + vm.sshKeyForEnvironment + " environment!", "success");
			ngDialog.closeAll();
		}).error(function (data) {
			SweetAlert.swal("Cancelled", "Error: " + data.ERROR, "error");
			ngDialog.closeAll();
		});
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

