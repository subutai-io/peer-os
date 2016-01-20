'use strict';

angular.module('subutai.plugin_integrator.controller', [])
	.controller('IntegratorCtrl', IntegratorCtrl)
	.directive('fileModel', fileModel);
fileModel.$inject = ["$parse"];

var karUploader = {};
IntegratorCtrl.$inject = ['$scope', 'IntegratorSrv', 'ngDialog', 'SweetAlert'];
function IntegratorCtrl($scope, IntegratorSrv, ngDialog, SweetAlert) {

	var vm = this;
	vm.newPlugin = {};
	vm.currentPlugin = {};
	vm.isNew = false;
	vm.step = "upload";
	vm.permissions = [
		{
			'object': 2,
			'name': 'Peer-Management',
			'scope': 1,
			'read': true,
			'write': true,
			'update': true,
			'delete': true,
		},
		{
			'object': 3,
			'name': 'Environment-Management',
			'scope': 1,
			'read': true,
			'write': true,
			'update': true,
			'delete': true,
		},
		{
			'object': 4,
			'name': 'Resource-Management',
			'scope': 1,
			'read': true,
			'write': true,
			'update': true,
			'delete': true,
		},
		{
			'object': 5,
			'name': 'Template-Management',
			'scope': 1,
			'read': true,
			'write': true,
			'update': true,
			'delete': true,
		}
	];
	vm.permissions2Add = [];


	vm.installedPlugins = [];
	vm.uploadPluginWindow = uploadPluginWindow;
	vm.getInstalledPlugins = getInstalledPlugins;
	vm.deletePlugin = deletePlugin;
	vm.editPermissionsWindow = editPermissionsWindow;
	vm.addPermission2Stack = addPermission2Stack;
	vm.removePermissionFromStack = removePermissionFromStack;
	vm.editPermissions = editPermissions;
	vm.uploadPlugin = uploadPlugin;



	function addPermission2Stack(permission) {
		vm.permissions2Add.push(angular.copy(permission));
		for (var i = 0; i < vm.permissions.length; ++i) {
			if (vm.permissions[i].name === permission.name) {
				vm.permissions.splice (i, 1);
				break;
			}
		}
	}

	function removePermissionFromStack(key) {
		vm.permissions.push (vm.permissions2Add[key]);
		vm.permissions2Add.splice(key, 1);
	}

	function editPermissions() {
		console.log (vm.permissions2Add);
		var postData = 'pluginId=' + vm.currentPlugin.id;

		if(vm.permissions2Add.length > 0) {
			postData += '&permission=' + JSON.stringify (vm.permissions2Add);
		}

		IntegratorSrv.editPermissions (postData).success(function (data) {
			ngDialog.closeAll();
			getInstalledPlugins();
			SweetAlert.swal ("Success!", "Your permissions were updated.", "success");
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Permission update error: " + error.replace(/\\n/g, " "), "error");
		});
	}

	function uploadPlugin() {
		ngDialog.closeAll();
		IntegratorSrv.uploadPlugin (vm.newPlugin.name, vm.newPlugin.version, karUploader, JSON.stringify(vm.permissions2Add)).success (function (data) {
			SweetAlert.swal ("Success!", "Your plugin was installed.", "success");
			vm.newPlugin = {};
			vm.permissions2Add = {};
			karUploader = {};
			getInstalledPlugins();
			ngDialog.closeAll();
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Plugin install error: " + error.replace(/\\n/g, " "), "error");
		});
	}






	function getInstalledPlugins() {
		vm.installedPlugins = [];
		IntegratorSrv.getInstalledPlugins().success (function (data) {
			vm.installedPlugins = data;
			console.log (data);
		});
	}
	getInstalledPlugins();


	function uploadPluginWindow() {
		vm.step = "upload";
		ngDialog.closeAll();
		ngDialog.open ({
			template: "subutai-app/plugin_integrator/partials/uploadPlugin.html",
			scope: $scope
		});
	}

	function editPermissionsWindow (plugin, isNew) {
		vm.permissions = [
			{
				'object': 2,
				'name': 'Peer-Management',
				'scope': 1,
				'read': true,
				'write': true,
				'update': true,
				'delete': true,
			},
			{
				'object': 3,
				'name': 'Environment-Management',
				'scope': 1,
				'read': true,
				'write': true,
				'update': true,
				'delete': true,
			},
			{
				'object': 4,
				'name': 'Resource-Management',
				'scope': 1,
				'read': true,
				'write': true,
				'update': true,
				'delete': true,
			},
			{
				'object': 5,
				'name': 'Template-Management',
				'scope': 1,
				'read': true,
				'write': true,
				'update': true,
				'delete': true,
			}
		];
		if (isNew) {
			for (var i = 0; i < vm.installedPlugins.length; ++i) {
				if (vm.installedPlugins[i].name === plugin.name) {
					SweetAlert.swal ("ERROR!", "Plugin with such name already exists", "error");
					return;
				}
			}
			vm.permissions2Add = [];
		}
		else {
			vm.currentPlugin = plugin;
			IntegratorSrv.getPermissions (vm.currentPlugin.id).success (function (data) {
				vm.permissions2Add = data.permissions;
				for(var i = 0; i < vm.permissions2Add.length; i++) {
					for(var j = 0; j < vm.permissions.length; j++) {
						if(vm.permissions[j].object === vm.permissions2Add[i].object) {
							vm.permissions2Add[i].name = vm.permissions[j].name;
							vm.permissions.splice (j, 1);
							--j
							break;
						}
					}
				}
				ngDialog.open ({
					template: "subutai-app/plugin_integrator/partials/uploadPlugin.html",
					scope: $scope
				});
			});
		}
		vm.isNew = isNew;
		vm.step = "perms";
	}


	function deletePlugin (plugin) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this plugin!",
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
				IntegratorSrv.deletePlugin (plugin.id).success (function (data) {
					SweetAlert.swal ("Success!", "Your plugin was deleted.", "success");
					getInstalledPlugins();
					ngDialog.closeAll();
				}).error (function (error) {
					SweetAlert.swal ("ERROR!", "Plugin delete error: " + error.replace(/\\n/g, " "), "error");
				});
			}
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
				scope.$apply(function(){
					modelSetter(scope, element[0].files[0]);
					if (element[0].files[0].name.substring (element[0].files[0].name.length - 4, element[0].files[0].name.length) !== ".kar") {
						document.getElementById ("filename").value = "Wrong file type";
						document.getElementById ("filename").style.color = "red";
					}
					else {
						karUploader = element[0].files[0];
						console.log (karUploader);
						document.getElementById ("filename").value = karUploader.name;
						document.getElementById ("filename").style.color = "#04346E";
					}
				});
			});
		}
	};
}
