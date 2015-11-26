'use strict';

angular.module('subutai.login.service', [])
.factory('loginSrv', loginSrv);

loginSrv.$inject = ['$http'];

function loginSrv($http)
{
	var LOGIN_URL = SERVER_URL + 'login';

	var loginSrv = {
		login: login
	};

	return loginSrv;


	function login( user, pass )
	{
		var postData =
			'username=' + user +
			'&password=' + pass;
		return $http.post(LOGIN_URL, postData, {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}});
	}
}
