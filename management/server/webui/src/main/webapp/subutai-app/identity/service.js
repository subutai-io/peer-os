'use strict';

angular.module('subutai.identity.service', [])
	.factory('identitySrv', identitySrv);


identitySrv.$inject = ['$http'];

function identitySrv($http) {
	var BASE_URL = serverUrl + 'identity_ui/';
	var usersURL = BASE_URL;
	var rolesURL = BASE_URL + 'roles/';
	var tokenURL = BASE_URL + 'users/tokens/';

	var identitySrv = {
		getTokens: getTokens,
		addToken: addToken,
		editToken: editToken,
		deleteToken: deleteToken,
		getUsers: getUsers,
		addUser : addUser,
		deleteUser: deleteUser,
		getRoles: getRoles,
		addRole: addRole,
		deleteRole: deleteRole
	};

	return identitySrv;

	//// Implementation
	
	function getTokens() {
		return $http.get(tokenURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function addToken(token) {
		var postData = 'token=' + token.token + '&period=' + token.period + '&userId=' + token.userId;
		return $http.post(
			tokenURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function editToken(token) {
		var postData = 'token=' + token.token 
			+ '&period=' + token.period 
			+ '&userId=' + token.userId 
			+ '&newToken=' + token.newToken;
		return $http.put(
			tokenURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}	

	function deleteToken(token) {
		return $http.delete(tokenURL + token);
	}

	function getUsers() {
		return $http.get(usersURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function addUser(postData) {
		return $http.post(
			usersURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteUser(userId) {
		return $http.delete(usersURL + userId);
	}

	function getRoles() {
		return $http.get(rolesURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function addRole(postData) {
		return $http.post(
			rolesURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteRole(roleId) {
		return $http.delete(rolesURL + roleId);
	}

}
