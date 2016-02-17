'use strict';

angular.module('subutai.login.controller', [])
	.controller('LoginCtrl', LoginCtrl)
	.controller('ChangePassCtrl', ChangePassCtrl)
	.directive('pwCheck', pwCheck);

LoginCtrl.$inject = ['loginSrv', '$http', '$location', '$rootScope', '$state'];
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
