'use strict';

angular.module('subutai.identity-user.controller', [])
	.controller('IdentityUserCtrl', IdentityUserCtrl)
	.controller('IdentityUserFormCtrl', IdentityUserFormCtrl)
	.directive('pwCheck', pwCheck)
	.directive('colSelect', colSelect)
	.directive('colSelect2', colSelect2);

IdentityUserCtrl.$inject = ['$scope', 'identitySrv', 'SweetAlert', 'ngDialog'];
IdentityUserFormCtrl.$inject = ['$scope', 'identitySrv', 'ngDialog'];

function userPostData(user) {
	var currentUserRoles = JSON.stringify(user.roles);
	var postData = 'username=' + user.userName + 
		'&full_name=' + user.fullName +
		'&password=' + user.password +
		'&email=' + user.email +
		'&public_key=' + user.public_key;

	if(currentUserRoles !== undefined) {
		postData += '&roles=' + currentUserRoles;
	}

	if(user.id !== undefined && user.id > 0) {
		postData += '&user_id=' + user.id;
	}

	return postData;
}

function IdentityUserCtrl($scope, identitySrv, SweetAlert, ngDialog) {

	var vm = this;

	//functions
	vm.userForm = userForm;
	vm.deleteUser = deleteUser;
	vm.removeRoleFromUser = removeRoleFromUser;
	vm.activeTab = "approved";

	function userForm(user) {
		if(user === undefined || user === null) user = false;
		identitySrv.getKey (user.securityKeyId).success (function (data) {
			user.public_key = data;
		});
		ngDialog.open({
			template: 'subutai-app/identityUser/partials/userForm.html',
			controller: 'IdentityUserFormCtrl',
			controllerAs: 'identityUserFormCtrl',
			data: user,
			preCloseCallback: function(value) {
				getUsers();
			}
		});
	}

	vm.users = [];
	vm.pendingUsers = [];
	function getUsers() {
		vm.users = [];
		vm.pendingUsers = [];
		identitySrv.getUsers().success (function (data) {
			console.log (data);
			for (var i = 0; i < data.length; ++i) {
				if (data[i].isApproved) {
					vm.users.push (data[i]);
				}
				else {
					vm.pendingUsers.push (data[i]);
				}
			}
		});
	}
	getUsers();
	vm.roles = [];
	function getRolesFromAPI() {
		identitySrv.getRoles().success(function (data) {
			vm.roles = data;
		});
	}
	getRolesFromAPI();


	function removeRoleFromUser(user, roleKey) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: 'Remove "' + user.roles[roleKey].name + '" role from user "' + user.userName + '"!',
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
				user.roles.splice(roleKey, 1);
				var userRoles = [];
				for(var i = 0; i < user.roles.length; i++) {
					userRoles.push(user.roles[i].id);
				}
				user.roles = userRoles;
				var postData = userPostData(user);
				console.log(postData);
				identitySrv.addUser(postData).success(function (data) {
					SweetAlert.swal("Removed!", "Role has been removed.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "User role is safe :). Error: " + data, "error");
				});
			}
		});
	}

	function deleteUser(user) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "You will not be able to recover this user!",
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
				identitySrv.deleteUser(user.id).success(function (data) {
					SweetAlert.swal("Deleted!", "User has been deleted.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "User is safe :). Error: " + data, "error");
				});
			}
		});
	}

	vm.approveWindow = approveWindow;
	vm.reject = reject;
	vm.colSelectUserRole = colSelectUserRole;
	vm.approve = approve;
	vm.currentUser = {};

	function approveWindow (user) {
		vm.currentUser = user;
		ngDialog.open({
			template: 'subutai-app/identityUser/partials/approveWindow.html',
			scope: $scope
		});
	}

	function approve() {
		identitySrv.approve (vm.currentUser.userName, JSON.stringify (vm.currentUser.roles)).success (function (data) {
			SweetAlert.swal ("Success!", "User was approved.", "success");
			getUsers();
			ngDialog.closeAll();
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "User approve error: " + error.replace(/\\n/g, " "), "error");
		});
	}

	function colSelectUserRole(id) {

		if(vm.currentUser.roles === undefined) {
			vm.currentUser.roles = [];
		}

		if(vm.currentUser.roles.indexOf(id) >= 0) {
			vm.currentUser.roles.splice(vm.currentUser.roles.indexOf(id), 1);
		} else {
			vm.currentUser.roles.push(id);
		}
	}

	function reject (user) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to undo this!",
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
				identitySrv.reject (user.id).success (function (data) {
					SweetAlert.swal ("Success!", "User was rejected.", "success");
					getUsers();
				}).error (function (error) {
					SweetAlert.swal ("ERROR!", "User reject error: " + error.replace(/\\n/g, " "), "error");
				});
			}
		});
	}

	function getKey (id) {
		return identitySrv.getKey (id);
	}

};

function IdentityUserFormCtrl($scope, identitySrv, ngDialog) {

	var vm = this;
	vm.user2Add = {};
	vm.roles = [];
	vm.currentUserRoles = [];
	vm.editUserName = false;

	//functions
	vm.addUser = addUser;
	vm.colSelectUserRole = colSelectUserRole;

	if($scope.ngDialogData !== undefined) {
		vm.user2Add = $scope.ngDialogData;
		vm.editUser = true;
		vm.currentUserRoles = angular.copy(vm.user2Add.roles);
		vm.user2Add.confirm_password = angular.copy(vm.user2Add.password);

		vm.user2Add.roles = [];
		for(var i = 0; i < vm.currentUserRoles.length; i++) {
			vm.user2Add.roles.push(vm.currentUserRoles[i].id);
		}
	}

	function getRolesFromAPI() {
		identitySrv.getRoles().success(function (data) {
			vm.roles = data;
		});
	}
	getRolesFromAPI();

	function addUser() {
		if ($scope.addUserForm.$valid) {
			var postData = userPostData(vm.user2Add);
			identitySrv.addUser(postData).success(function (data) {
				ngDialog.closeAll();
			});
			$scope.addUserForm.$setPristine();
			$scope.addUserForm.$setUntouched();
		}		
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
	}

}

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

function colSelect2() {
	return {
		restrict: 'E',
		templateUrl: 'subutai-app/identityUser/directives/col-select/col-select-roles2.html'
	}
};

