'use strict';

angular.module('subutai.login.controller', [])
	.controller('LoginCtrl', LoginCtrl);

LoginCtrl.$inject = ['loginSrv', '$http', '$location', '$rootScope'];

function LoginCtrl( loginSrv, $http, $location, $rootScope )
{
	var vm = this;

	vm.name = "";
	vm.pass = "";

	//functions
	vm.login = login;

	function login() {
		loginSrv.login( vm.name, vm.pass ).success(function(data){
			sessionStorage.setItem('currentUser', vm.name);
			$rootScope.currentUser = vm.name;
			$http.defaults.headers.common['sptoken']= getCookie('sptoken');
			$location.path('/');
		});
	}
}
