'use strict';

angular.module('subutai.login.controller', [])
	.controller('LoginCtrl', LoginCtrl);

LoginCtrl.$inject = ['$http', '$cookieStore', 'loginSrv'];

function LoginCtrl( $http, $cookieStore, loginSrv )
{
	var vm = this;

	vm.name = "";
	vm.pass = "";

	//functions
	vm.login = login;

	function login()
	{
		$cookieStore.remove('sptoken');
		$http.defaults.headers.common['sptoken'] = "";
		loginSrv.login( vm.name, vm.pass).success(function( data ) {

		});
	}
}
