'use strict';

angular.module('subutai.login.controller', [])
	.controller('LoginCtrl', LoginCtrl)
	.controller('SignupCtrl', SignupCtrl)
	.controller('ChangePassCtrl', ChangePassCtrl)
	.directive('pwCheck', pwCheck);

LoginCtrl.$inject = ['loginSrv', '$http', '$location', '$rootScope', '$state'];
SignupCtrl.$inject = ['ngDialog', '$http', '$scope', 'SweetAlert'];
ChangePassCtrl.$inject = ['$scope', 'loginSrv', '$http', '$location', '$rootScope', '$state'];

function ChangePassCtrl( $scope, loginSrv, $http, $location, $rootScope, $state) {
	var vm = this;

	vm.changePass = changePass;

	function changePass(newPassword) {
		if ($scope.changePassForm.$valid) {		
			console.log(newPassword);
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

function LoginCtrl( loginSrv, $http, $location, $rootScope, $state )
{
	var vm = this;

	vm.name = "";
	vm.pass = "";
	vm.errorMessage = false;

	//functions
	vm.login = login;

	function login() {
		loginSrv.login( vm.name, vm.pass ).success(function(data){
			localStorage.setItem('currentUser', vm.name);
			$rootScope.currentUser = vm.name;
			$http.defaults.headers.common['sptoken']= getCookie('sptoken');
			//$location.path('');
			$state.go('home');
		}).error(function(error){
			console.log(error);
			vm.errorMessage = error;
		});
	}
}


function SignupCtrl (ngDialog, $http, $scope, SweetAlert) {
	var vm = this;
	vm.user2Add = {};
	vm.signUpWindow = signUpWindow;
	vm.requestNewUser = requestNewUser;

	function signUpWindow() {
		vm.user2Add = {};
		ngDialog.open ({
			template: "subutai-app/login/partials/signUp.html",
			scope: $scope
		});
	}

	//functions
	function requestNewUser() {
		console.log ("!");
		var postData = "username=" + vm.user2Add.username + "&full_name=" + vm.user2Add.fullName + "&password=" + vm.user2Add.password + "&email=" + vm.user2Add.email + "&public_key=" + encodeURIComponent(vm.user2Add.public_key);
		$http.post(
			SERVER_URL + 'rest/ui/identity/signup',
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}})
		.success(function (data) {
			SweetAlert.swal ("Success!", "Your request was sent.", "success");
			vm.user2Add = {};
			ngDialog.closeAll();
		}).error (function (error) {
			SweetAlert.swal ("ERROR!", "Signup error: " + error.replace(/\\n/g, " "), "error");
		});
	}
}
