'use strict';

angular.module('subutai.identity-role.controller', [])
	.controller('IdentityRoleCtrl', IdentityRoleCtrl);

IdentityRoleCtrl.$inject = ['$scope', 'identitySrv', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'SweetAlert'];

function IdentityRoleCtrl($scope, identitySrv, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, SweetAlert) {

	var vm = this;

	var permissionIdentityManagement = {
		"object": 1,
		"name": "Identity-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	};

	var permissionPeerManagement = {
		"object": 2,
		"name": "Peer-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	};

	var permissionEnvironmentManagement = {
		"object": 3,
		"name": "Environment-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	};

	var permissionResourceManagement = {
		"object": 4,
		"name": "Resource-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	};

	var permissionTemplateManagement = {
		"object": 5,
		"name": "Template-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	};

	var permissionKarafServerAdministration = {
		"object": 6,
		"name": "Karaf-Server-Administration",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	};

	var permissionKarafServerManagement = {
		"object": 7,
		"name": "Karaf-Server-Management",
		"scope": 1,
		"read": true,
		"write": true,
		"update": true,
		"delete": true,
		"selected": false
	};

	vm.permissionsDefault = [];
	vm.permissionsDefault[permissionIdentityManagement.object] = permissionIdentityManagement;
	vm.permissionsDefault[permissionPeerManagement.object] = permissionPeerManagement;
	vm.permissionsDefault[permissionEnvironmentManagement.object] = permissionEnvironmentManagement;
	vm.permissionsDefault[permissionResourceManagement.object] = permissionResourceManagement;
	vm.permissionsDefault[permissionTemplateManagement.object] = permissionTemplateManagement;
	vm.permissionsDefault[permissionKarafServerAdministration.object] = permissionKarafServerAdministration;
	vm.permissionsDefault[permissionKarafServerManagement.object] = permissionKarafServerManagement;

	vm.permissions2Add = angular.copy(vm.permissionsDefault);
	vm.role2Add = {}

	//functions
	vm.addPermission2Stack = addPermission2Stack;
	vm.editRole = editRole;
	vm.addRole = addRole;
	vm.deleteRole = deleteRole;
	vm.removePermissionFromRole = removePermissionFromRole;

	vm.dtInstance = {};
	vm.roles = {};
	vm.dtOptions = DTOptionsBuilder.fromFnPromise(function() {
		return $resource(serverUrl + 'identity_ui/roles/').query().$promise;
	}).withPaginationType('full_numbers').withOption('createdRow', createdRow);
	vm.dtColumns = [
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEdit),
		DTColumnBuilder.newColumn('name').withTitle('Role'),
		DTColumnBuilder.newColumn(null).withTitle('Role permissions').renderWith(permissionsTags),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionDelete)
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function actionEdit(data, type, full, meta) {
		vm.roles[data.id] = data;
		return '<a href class="b-icon b-icon_edit" ng-click="identityRoleCtrl.editRole(identityRoleCtrl.roles[' + data.id + '])"></a>';
	}	

	function permissionsTags(data, type, full, meta) {
		var permissionsHTML = '';
		for(var i = 0; i < data.permissions.length; i++) {
			permissionsHTML += '<span class="b-tags b-tags_grey">' 
				+ vm.permissionsDefault[data.permissions[i].object].name 
				+ ' <a href ng-click="identityRoleCtrl.removePermissionFromRole(identityRoleCtrl.roles[' + data.id + '], ' + i + ')"><i class="fa fa-times"></i></a>' 
			+ '</span>';
		}
		return permissionsHTML;
	}

	function actionDelete(data, type, full, meta) {
		return '<a href class="b-icon b-icon_remove" ng-click="identityRoleCtrl.deleteRole(' + data.id + ')"></a>';
	}

	function addPermission2Stack(permission) {
		permission.selected = !permission.selected;
	}

	function editRole(role) {
		vm.permissions2Add = angular.copy(vm.permissionsDefault);
		for(var i = 0; i < role.permissions.length; i++) {
			for(var j = 0; j < vm.permissions2Add.length; j++) {
				if(vm.permissions2Add[j].object == role.permissions[i].object) {
					vm.permissions2Add[j].selected = true;
					vm.permissions2Add[j].read = role.permissions[i].read;
					vm.permissions2Add[j].write = role.permissions[i].write;
					vm.permissions2Add[j].update = role.permissions[i].update;
					vm.permissions2Add[j].delete = role.permissions[i].delete;
					break;
				}
			}
		}
		vm.role2Add = angular.copy(role);
	}

	function removePermissionFromRole(role, permissionKey) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: 'Remove "' + vm.permissionsDefault[role.permissions[permissionKey].object].name + '" permission from role!',
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#DD6B55",
			confirmButtonText: "Yes, remove it!",
			cancelButtonText: "No, cancel!",
			closeOnConfirm: false,
			closeOnCancel: false,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {

				role.permissions.splice(permissionKey, 1);

				var postData = 'rolename=' + role.name;
				if(role.id !== undefined && role.id > 0) {
					postData += '&role_id=' + role.id;
				}
				postData += '&permission=' + JSON.stringify(role.permissions);
				console.log(postData);

				identitySrv.addRole(postData).success(function (data) {
					SweetAlert.swal("Removed!", "Permission has been removed.", "success");
					vm.dtInstance.reloadData();
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Role permission is safe :). Error: " + data, "error");
				});
			} else {
				SweetAlert.swal("Cancelled", "Role permission is safe :)", "error");
			}
		});
	}

	function deleteRole(roleId) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this role!",
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
				identitySrv.deleteRole(roleId).success(function (data) {
					SweetAlert.swal("Deleted!", "Role has been deleted.", "success");
					vm.dtInstance.reloadData();
				});
			} else {
				SweetAlert.swal("Cancelled", "Role is safe :)", "error");
			}
		});		
	}

	function addRole() {
		if(vm.role2Add.name === undefined || vm.role2Add.name.length < 1) return;

		var postData = 'rolename=' + vm.role2Add.name;
		if(vm.role2Add.id !== undefined && vm.role2Add.id > 0) {
			postData += '&role_id=' + vm.role2Add.id;
		}

		var permissionsArray = [];
		for(var i = 0; i < vm.permissions2Add.length; i++) {
			if(vm.permissions2Add[i].selected === true) {
				permissionsArray.push(vm.permissions2Add[i]);
			}
		}

		if(permissionsArray.length > 0) {
			postData += '&permission=' + JSON.stringify(permissionsArray);
		}

		identitySrv.addRole(postData).success(function (data) {
			vm.dtInstance.reloadData();
		});
	}	

};

