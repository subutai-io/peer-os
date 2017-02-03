'use strict';

angular.module('subutai.login.service', [])
.factory('loginSrv', loginSrv);

loginSrv.$inject = ['$http'];

function loginSrv($http)
{
	var LOGIN_URL = SERVER_URL + 'login';

	var loginSrv = {
		login: login,
		changePass: changePass,
		getHubIp: getHubIp
	};

	return loginSrv;


	function login( postData ) {
		return $http.post(LOGIN_URL, postData, {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}});
	}

    function getHubIp() {
		return $http.get(SERVER_URL + '/rest/v1/system/hub_ip', {withCredentials: true, headers: {'Content-Type': 'application/json'}});
    }

	function changePass (postData) {
		return $http.post(
			LOGIN_URL,
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
