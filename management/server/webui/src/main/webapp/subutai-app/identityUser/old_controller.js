'use strict';

angular.module('subutai.identity-user.controller', [])
	.controller('IdentityUserCtrl', IdentityUserCtrl)
	.directive('pwCheck', pwCheck)
	.directive('colSelect', colSelect);	

IdentityUserCtrl.$inject = ['$scope', 'identitySrv', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'SweetAlert'];

function IdentityUserCtrl($scope, identitySrv, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, SweetAlert) {

	var vm = this;
	vm.user2Add = {};
	vm.editUserName = true;

	vm.permissionsDefault = [
		{
			"object": 1,
			"name": "Identity-Management",
			"scope": 1,
			"read": true,
			"write": true,
			"update": true,
			"delete": true,
			"selected": false
		},
		{
			"object": 2,
			"name": "Peer-Management",
			"scope": 1,
			"read": true,
			"write": true,
			"update": true,
			"delete": true,
			"selected": false
		},
		{
			"object": 3,
			"name": "Environment-Management",
			"scope": 1,
			"read": true,
			"write": true,
			"update": true,
			"delete": true,
			"selected": false
		},
		{
			"object": 4,
			"name": "Resource-Management",
			"scope": 1,
			"read": true,
			"write": true,
			"update": true,
			"delete": true,
			"selected": false
		},
		{
			"object": 5,
			"name": "Template-Management",
			"scope": 1,
			"read": true,
			"write": true,
			"update": true,
			"delete": true,
			"selected": false
		},
		{
			"object": 6,
			"name": "Karaf-Server-Administration",
			"scope": 1,
			"read": true,
			"write": true,
			"update": true,
			"delete": true,
			"selected": false
		},
		{
			"object": 7,
			"name": "Karaf-Server-Management",
			"scope": 1,
			"read": true,
			"write": true,
			"update": true,
			"delete": true,
			"selected": false
		}
	];

	vm.permissions2Add = angular.copy(vm.permissionsDefault);
	vm.role2Add = {}

	//functions
	vm.addPane = addPane;
	vm.closePane = closePane;
	vm.addUser = addUser;
	vm.editUser = editUser;
	vm.deleteUser = deleteUser;
	vm.colSelectUserRole = colSelectUserRole;
	vm.addNewPanel = addNewPanel;
	vm.addPermission2Stack = addPermission2Stack;
	vm.editRole = editRole;
	vm.addRole = addRole;
	vm.deleteRole = deleteRole;

	vm.isUser = true;
	vm.isRole = false;

	vm.dtInstance = {};
	vm.users = {};
	vm.dtOptions = DTOptionsBuilder.fromFnPromise(function() {
		return $resource('http://172.16.131.201:8181/rest/identity_ui/').query().$promise;
	}).withPaginationType('full_numbers').withOption('createdRow', createdRow);
	vm.dtColumns = [
		DTColumnBuilder.newColumn('id').withTitle('ID'),
		DTColumnBuilder.newColumn('userName').withTitle('Username'),
		DTColumnBuilder.newColumn('fullName').withTitle('Full name'),
			DTColumnBuilder.newColumn(null).withTitle('Actions').notSortable()
				.renderWith(actionsHtml)
	];

	function getRolesFromAPI() {
		identitySrv.getRoles().success(function (data) {
			vm.roles = data;
		});
	}
	getRolesFromAPI();

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function actionsHtml(data, type, full, meta) {
		vm.users[data.id] = data;
		return '<button class="btn btn-warning" ng-click="identityUserCtrl.editUser(identityUserCtrl.users[' + data.id + '])">' +
			'   <i class="fa fa-edit"></i>' +
			'</button>&nbsp;' +
			'<button class="btn btn-danger" ng-click="identityUserCtrl.deleteUser(identityUserCtrl.users[' + data.id + '])">' +
			'   <i class="fa fa-trash-o"></i>' +
			'</button>';
	}

	function addPermission2Stack(permission) {
		permission.selected = !permission.selected;
	}

	function editRole(role) {
		vm.permissions2Add = angular.copy(vm.permissionsDefault);
		console.log(role);
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
		//vm.role2Add = role;
		addPane();
	}

	function deleteRole(roleId, key) {
		SweetAlert.swal(
				{
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
							vm.roles.splice(key, 1);
							SweetAlert.swal("Deleted!", "Role has been deleted.", "success");
						});
					} else {
						SweetAlert.swal("Cancelled", "Role is safe :)", "error");
					}
				}
		);		
	}

	function addUser() {
		//vm.users.push(angular.copy(vm.person2Add));
		if ($scope.addUserForm.$valid) {
			var currentUserRoles = JSON.stringify(vm.user2Add.roles);
			var postData = 'username=' + vm.user2Add.userName + 
				'&full_name=' + vm.user2Add.fullName +
				'&password=' + vm.user2Add.password +
				'&email=' + vm.user2Add.email;

			if(currentUserRoles !== undefined) {
				postData += '&roles=' + currentUserRoles;
			}

			if(vm.user2Add.id !== undefined && vm.user2Add.id > 0) {
				postData += '&user_id=' + vm.user2Add.id;
			}

			console.log(postData);
			identitySrv.addUser(postData).success(function (data) {
				vm.dtInstance.reloadData();
			});
			$scope.addUserForm.$setPristine();
			$scope.addUserForm.$setUntouched();
		}		
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
		console.log(postData);

		identitySrv.addRole(postData).success(function (data) {
			getRolesFromAPI();
		});
	}	

	function colSelectUserRole(id) {

		if(vm.user2Add.roles === undefined) {
			vm.user2Add.roles = [];
		}

		if(vm.user2Add.roles.indexOf(id) >= 0) {
			vm.user2Add.roles.splice(vm.user2Add.roles.indexOf(id), 1);
		} else {
			vm.user2Add.roles.push(id);
		}
		console.log(vm.user2Add.roles);
	}

	function addNewPanel() {
		if( vm.isUser ) {
			vm.user2Add = {};
			vm.editUserName = true;
		} else if( vm.isRole ) {
			vm.role2Add = {};
			vm.permissions2Add = angular.copy(vm.permissionsDefault);
		}
		addPane();
	}

	function editUser(user) {
		//vm.message = 'You are trying to edit the row: ' + JSON.stringify(user);
		vm.editUserName = false;
		vm.user2Add = angular.copy(user);
		vm.user2Add.confirm_password = angular.copy(vm.user2Add.password);
		vm.user2Add.roles = [];
		for(var i = 0; i < user.roles.length; i++) {
			vm.user2Add.roles.push(user.roles[i].id);
		}
		addPane();
	}

	function deleteUser(user) {
		//vm.message = 'You are trying to remove the row: ' + JSON.stringify(user);
		SweetAlert.swal(
				{
					title: "Are you sure?",
					text: "Your will not be able to recover this user!",
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
						SweetAlert.swal("Deleted!", "User has been deleted.", "success");
						identitySrv.deleteUser(user.id).success(function (data) {
							vm.dtInstance.reloadData();
						});
					} else {
						SweetAlert.swal("Cancelled", "User is safe :)", "error");
					}
				}
		);
	}

	//// Implementation

	function addPane(z) {
		jQuery('#resizable-pane').removeClass('fullWidthPane');
		if( vm.isUser ) {
			jQuery('#role-form').css('display', 'none');
			jQuery('#user-form').css('display', 'block');
			jQuery('#role-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#user-form').removeClass('bounceOutRight');
			jQuery('#user-form').addClass('animated bounceInRight');
		}
		else if( vm.isRole ) {
			jQuery('#user-form').css('display', 'none');
			jQuery('#role-form').css('display', 'block');
			jQuery('#user-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#role-form').removeClass('bounceOutRight');
			jQuery('#role-form').addClass('animated bounceInRight');
		}
	}

	function closePane() {
		jQuery('#resizable-pane').addClass('fullWidthPane');
		if( vm.isUser ) {
			jQuery('#user-form').addClass('bounceOutRight');
			jQuery('#user-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#user-form').css('display', 'none');
		}
		else if( vm.isRole ) {
			jQuery('#role-form').addClass('bounceOutRight');
			jQuery('#role-form').removeClass('animated bounceOutRight bounceInRight');
			jQuery('#role-form').css('display', 'none');
		}
	}
};

function pwCheck() {
	return {
		require: 'ngModel',
		link: function (scope, elem, attrs, ctrl) {
			var firstPassword = '#' + attrs.pwCheck;
			elem.add(firstPassword).on('keyup', function () {
				scope.$apply(function () {
					ctrl.$setValidity('pwmatch', elem.val() === $(firstPassword).val());
				});
			});
		}
	}
};

function colSelect() {
	return {
		restrict: 'E',
		templateUrl: 'subutai-app/identityUser/directives/col-select/col-select-roles.html'
	}
};

