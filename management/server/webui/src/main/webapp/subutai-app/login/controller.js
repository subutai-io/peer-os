'use strict';

angular.module('subutai.login.controller', [])
	.controller('LoginCtrl', LoginCtrl);

LoginCtrl.$inject = ['loginSrv'];

function LoginCtrl( loginSrv )
{
	var vm = this;

	vm.name = "";
	vm.pass = "";

	//functions
	vm.login = login;

	function login()
	{
		loginSrv.login( vm.name, vm.pass );
	}
}
