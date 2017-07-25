'use strict';

angular.module('subutai.identity-user.controller', [])
	.controller('IdentityUserCtrl', IdentityUserCtrl)
	.directive('pwCheck', pwCheck)
	.directive('colSelect', colSelect);
IdentityUserCtrl.$inject = ['$scope', 'identitySrv', 'SweetAlert', 'ngDialog', 'cfpLoadingBar', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile'];

var trustedLevels = {
	1: "Never Trust",
	2: "Marginal",
	3: "Full",
	4: "Ultimate Trust"
};

function userPostData(user) {
	var currentUserRoles = JSON.stringify(user.roles);
	var postData = 'username=' + user.userName +
		'&full_name=' + user.fullName +
		'&password=' + user.password +
		'&email=' + user.email +
		'&trustLevel=' + user.trustLevel;

	if(currentUserRoles !== undefined) {
		postData += '&roles=' + currentUserRoles;
	}

	if(user.id !== undefined && user.id > 0) {
		postData += '&user_id=' + user.id;
	}

	return postData;
}

function IdentityUserCtrl($scope, identitySrv, SweetAlert, ngDialog, cfpLoadingBar, DTOptionsBuilder, DTColumnBuilder, $resource, $compile) {

	var vm = this;

	vm.user2Add = {"trustLevel": 3};
	vm.users = [];
	vm.roles = [];
	vm.currentUserRoles = [];
	vm.editUserName = false;
	vm.activeTab = "approved";
	vm.addUserForm = '';

	vm.userTypes = {
		1: "System",
		2: "Regular",
	};

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	//functions
	vm.userForm = userForm;
	vm.deleteUser = deleteUser;
	vm.removeRoleFromUser = removeRoleFromUser;
	vm.addUser = addUser;
	vm.colSelectUserRole = colSelectUserRole;
	vm.selectAll = selectAll;
	vm.unselectAll = unselectAll;
	vm.loginPatern = '(?=^.{4,}$)(^(?!(admin|sys|token)).*)';

	vm.trustedLevels = trustedLevels;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.dtInstance = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( identitySrv.getUsersUrl() ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('stateSave', true)
		.withOption('order', [[ 1, "asc" ]])
		.withOption('rowCallback', function(row, data, index){
			if(data.trustLevel == 1) {
				$(row).addClass('b-untrusted-user');
			} else if(data.trustLevel == 2) {
				$(row).addClass('b-midletrusted-user');
			} else {
				$(row).addClass('b-trusted-user');
			}
		})
		.withOption('createdRow', createdRow);

	vm.dtColumns = [
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionEdit),
		DTColumnBuilder.newColumn('userName').withTitle('Username'),
//		DTColumnBuilder.newColumn('type').withTitle('User type').renderWith(getUserType),
//		DTColumnBuilder.newColumn('trustLevel').withTitle('Trust Level').renderWith(getUserTrustLevel),
//		DTColumnBuilder.newColumn(null).withTitle('Roles').renderWith(rolesTags),
		DTColumnBuilder.newColumn('fullName').withTitle('Full name'),
		DTColumnBuilder.newColumn('email').withTitle('E-mail'),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionDelete)
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function actionEdit(data, type, full, meta) {
		vm.users[data.id] = data;
		return '<a href class="b-icon b-icon_edit" ng-click="identityUserCtrl.userForm(' + data.id + ')"></a>';
	}

	function getUserType(type) {
		return vm.userTypes[type];
	}

	function getUserTrustLevel(trustLevel) {
		return vm.trustedLevels[trustLevel];
	}

	function rolesTags(data, type, full, meta) {
		var rolesTagsHTML = '';
		for(var i = 0; i < data.roles.length; i++) {
			rolesTagsHTML += '<span class="b-tags b-tags_grey">' 
				+ data.roles[i].name 
				+ ' <a href ng-click="identityUserCtrl.removeRoleFromUser(' + data.id + ', ' + i + ')"><i class="fa fa-times"></i></a>' 
			+ '</span>';
		}
		return rolesTagsHTML;
	}

	function actionDelete(data, type, full, meta) {
		return '<a href class="b-icon b-icon_remove" ng-click="identityUserCtrl.deleteUser(' + data.id + ')"></a>';
	}	

	function userForm(userId) {
		if(userId === undefined || userId === null) userId = false;
		if(userId) {
			vm.user2Add = angular.copy(vm.users[userId]);
			vm.editUser = true;
			vm.loginPatern = '';
			vm.currentUserRoles = angular.copy(vm.user2Add.roles);
			vm.user2Add.password = '';
			//vm.user2Add.confirm_password = angular.copy(vm.user2Add.password);

			vm.user2Add.roles = [];
			for(var i = 0; i < vm.currentUserRoles.length; i++) {
				vm.user2Add.roles.push(vm.currentUserRoles[i].id);
			}
		} else {
			vm.editUser = false;
			vm.loginPatern = '(?=^.{4,}$)(^(?!(admin|sys|token)).*)';
			vm.user2Add = {"trustLevel": 3};
		}

		ngDialog.open({
			template: 'subutai-app/identityUser/partials/userForm.html',
			scope: $scope
		});
	}

	function getRolesFromAPI() {
		identitySrv.getRoles().success(function (data) {
			vm.roles = data;
		});
	}
	getRolesFromAPI();

	function addUser() {
		if (vm.addUserForm.$valid) {
			var postData = userPostData(vm.user2Add);
			LOADING_SCREEN();
			ngDialog.closeAll();
			identitySrv.addUser(postData).success(function (data) {
				vm.users[vm.user2Add.id] = vm.user2Add;
				LOADING_SCREEN('none');
				if(Object.keys(vm.dtInstance).length !== 0) {
					vm.dtInstance.reloadData(null, false);
				}
			}).error(function(error){
				LOADING_SCREEN('none');
				SweetAlert.swal ("ERROR!", "Error: " + error, "error");
			});
			vm.user2Add = {"trustLevel": 3};
			vm.addUserForm.$setPristine();
			vm.addUserForm.$setUntouched();
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

	function selectAll() {
		vm.user2Add.roles = [];
		for (var i = 0; i < vm.roles.length; i++) {
			vm.user2Add.roles.push(vm.roles[i].id);
		}
	}

	function unselectAll() {
		vm.user2Add.roles = [];
	}

	function removeRoleFromUser(userId, roleKey) {
		var user = angular.copy(vm.users[userId]);
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
					identitySrv.addUser(postData).success(function (data) {
						SweetAlert.swal("Removed!", "Role has been removed.", "success");
						vm.dtInstance.reloadData(null, false);
					}).error(function (error) {
						SweetAlert.swal("ERROR!", "User role is safe. Error: " + error, "error");
					});
				}
			});
	}

	function deleteUser(userId) {
		identitySrv.hasEnvironments(userId).success(function (data) {
            showConfirmationDialog(userId, data == true || data == 'true');
		});
	}

	function showConfirmationDialog(userId, hasEnvironments){
		var previousWindowKeyDown = window.onkeydown;
		SweetAlert.swal({
				title: "Are you sure?",
				text: ( hasEnvironments? "This user has environments! " : "" ) + "You will not be able to recover this user!",
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
				window.onkeydown = previousWindowKeyDown;
				if (isConfirm) {
					identitySrv.deleteUser(userId).success(function (data) {
						SweetAlert.swal("Deleted!", "User has been deleted.", "success");
						vm.dtInstance.reloadData(null, false);
					}).error(function (data) {
						SweetAlert.swal("ERROR!", "User is safe :). Error: " + data, "error");
					});
				}
			});
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

function colSelect2() {
	return {
		restrict: 'E',
		templateUrl: 'subutai-app/identityUser/directives/col-select/col-select-roles2.html'
	}
};

