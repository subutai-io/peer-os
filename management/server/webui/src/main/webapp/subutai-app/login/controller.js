'use strict';

angular.module('subutai.login.controller', [])
	.controller('LoginCtrl', LoginCtrl);

LoginCtrl.$inject = ['loginSrv', '$http'];

function LoginCtrl( loginSrv, $http )
{
	var vm = this;

	vm.name = "";
	vm.pass = "";

	//functions
	vm.login = login;

	function login() {
		loginSrv.login( vm.name, vm.pass ).success(function(data){
			$http.defaults.headers.common['sptoken']= getCookie('sptoken');
		});
	}
}
