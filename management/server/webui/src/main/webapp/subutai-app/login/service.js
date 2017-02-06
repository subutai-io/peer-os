'use strict';

angular.module('subutai.login.service', [])
.factory('loginSrv', loginSrv);

loginSrv.$inject = ['$http'];

function loginSrv($http)
{
	var LOGIN_URL = SERVER_URL + 'login';
	var USER_URL = SERVER_URL + 'rest/ui/identity/';

	var loginSrv = {
		login: login,
		changePass: changePass,
		resetPass: resetPass,
		getSignToken: getSignToken
	};

	return loginSrv;


	function login( postData ) {
		return $http.post(LOGIN_URL, postData, {withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}});
	}

    function getSignToken() {
		return $http.get(SERVER_URL + 'rest/v1/identity/signtoken', {withCredentials: true, headers: {'Content-Type': 'application/json'}});
    }

	function changePass (passObj) {
		var postData = "old=" + passObj.oldPassword + "&new=" + passObj.newPassword;
		return $http.post(
			USER_URL + "new-password",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function resetPass (postData) {
		return $http.post(
			LOGIN_URL,
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}
}
