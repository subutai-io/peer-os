'use strict';

angular.module('subutai.identity-role.controller', [])
	.controller('IdentityRoleCtrl', IdentityRoleCtrl)
	.controller('IdentityRoleFormCtrl', IdentityRoleFormCtrl);

IdentityRoleCtrl.$inject = ['$scope', 'identitySrv', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'SweetAlert', 'ngDialog'];
IdentityRoleFormCtrl.$inject = ['$scope', 'identitySrv', 'SweetAlert', 'ngDialog'];

function IdentityRoleCtrl($scope, identitySrv, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, SweetAlert, ngDialog) {

	var vm = this;

	vm.permissions2Add = angular.copy(permissionsDefault);
	vm.role2Add = {}

	//functions
	vm.roleForm = roleForm;
	vm.deleteRole = deleteRole;
	vm.removePermissionFromRole = removePermissionFromRole;

	function roleForm(role) {
		if(role === undefined || role === null) role = false;

		ngDialog.open({
			template: 'subutai-app/identityRole/partials/roleForm.html',
			controller: 'IdentityRoleFormCtrl',
			controllerAs: 'identityRoleFormCtrl',
			data: role,
			preCloseCallback: function(value) {
				vm.dtInstance.reloadData(null, false);
			}
		});
	}

	vm.dtInstance = {};
	vm.roles = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( identitySrv.getRolesUrl() ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('stateSave', true)
		.withOption('order', [[ 1, "asc" ]])
		.withOption('createdRow', createdRow);

	vm.dtColumns = [
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEdit),
		DTColumnBuilder.newColumn('name').withTitle('Roles'),
		DTColumnBuilder.newColumn(null).withTitle('Role permissions').renderWith(permissionsTags),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionDelete)
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function actionEdit(data, type, full, meta) {
		vm.roles[data.id] = data;
		return '<a href class="b-icon b-icon_edit" ng-click="identityRoleCtrl.roleForm(identityRoleCtrl.roles[' + data.id + '])"></a>';
	}	

	function permissionsTags(data, type, full, meta) {
		var permissionsHTML = '';
		for(var i = 0; i < data.permissions.length; i++) {
			var perrmission = $.grep(permissionsDefault, function(element, index) {
				return (element.object === data.permissions[i].object);
			})[0];
			permissionsHTML += '<span class="b-tags b-tags_grey">' 
				+ perrmission.name 
				+ ' <a href ng-click="identityRoleCtrl.removePermissionFromRole(identityRoleCtrl.roles[' + data.id + '], ' + i + ')"><i class="fa fa-times"></i></a>' 
			+ '</span>';
		}
		return permissionsHTML;
	}

	function actionDelete(data, type, full, meta) {
		return '<a href class="b-icon b-icon_remove" ng-click="identityRoleCtrl.deleteRole(' + data.id + ')"></a>';
	}

	function removePermissionFromRole(role, permissionKey) {
		var perrmission = $.grep(permissionsDefault, function(element, index) {
			return (element.object === role.permissions[permissionKey].object);
		})[0];
		SweetAlert.swal({
			title: "Are you sure?",
			text: 'Remove "' + perrmission.name + '" permission from role!',
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Remove",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
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

				identitySrv.addRole(postData).success(function (data) {
					SweetAlert.swal("Removed!", "Permission has been removed.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Role permission is safe :). Error: " + data, "error");
				});
			}
		});
	}

	function deleteRole(roleId) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "You will not be able to recover this Role!",
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
				identitySrv.deleteRole(roleId).success(function (data) {
					SweetAlert.swal("Deleted!", "Role has been deleted.", "success");
					vm.dtInstance.reloadData(null, false);
				});
			}
		});
	}

};

function IdentityRoleFormCtrl($scope, identitySrv, SweetAlert, ngDialog) {

	var vm = this;

	vm.permissions = angular.copy(permissionsDefault);
	vm.permissions2Add = [];
	vm.scopes = [];
	vm.role2Add = {}
	vm.editRole = false;

	if($scope.ngDialogData !== undefined) {
		//vm.role2Add = $scope.ngDialogData;
		vm.editRole = true;

		var role = $scope.ngDialogData;
		vm.permissions2Add = role.permissions;
		for(var i = 0; i < vm.permissions2Add.length; i++) {
			for(var j = 0; j < vm.permissions.length; j++) {
				if(vm.permissions[j].object == vm.permissions2Add[i].object) {
					vm.permissions2Add[i].name = vm.permissions[j].name;
					break;
				}
			}
		}
		vm.role2Add = role;
	}

	identitySrv.getPermissionsScops().success(function (data) {
		vm.scopes = data;
	});

	//functions
	vm.addPermission2Stack = addPermission2Stack;
	vm.removePermissionFromStack = removePermissionFromStack;
	vm.addRole = addRole;

	function addPermission2Stack(permission) {
		vm.permissions2Add.push(angular.copy(permission));
	}

	function removePermissionFromStack(key) {
		vm.permissions2Add.splice(key, 1);
	}

	function addRole() {
		if(vm.role2Add.name === undefined || vm.role2Add.name.length < 1) return;

		var postData = 'rolename=' + vm.role2Add.name;
		if(vm.role2Add.id !== undefined && vm.role2Add.id > 0) {
			postData += '&role_id=' + vm.role2Add.id;
		}

		if(vm.permissions2Add.length > 0) {
			postData += '&permission=' + JSON.stringify(vm.permissions2Add);
		}

		identitySrv.addRole(postData).success(function (data) {
			ngDialog.closeAll();
		});
	}
	
}

