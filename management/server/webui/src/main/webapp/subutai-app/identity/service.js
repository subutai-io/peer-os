'use strict';

angular.module('subutai.identity.service', [])
	.factory('identitySrv', identitySrv);


identitySrv.$inject = ['$http'];

function identitySrv($http) {
	var BASE_URL = SERVER_URL + 'rest/ui/identity/';
	var USERS_URL = BASE_URL;
	var ROLES_URL = BASE_URL + 'roles/';
	var TOKENS_URL = BASE_URL + 'users/tokens/';

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
		deleteRole: deleteRole,
		getTokenTypes: getTokenTypes,
		getPermissionsScops: getPermissionsScops,
		getCurrentUser: getCurrentUser,

		getUsersUrl : function(){ return USERS_URL },
		getRolesUrl : function(){ return ROLES_URL },
		getTokensUrl : function(){ return TOKENS_URL }
	};

	return identitySrv;

	//// Implementation

	
	function getTokens() {
		return $http.get(TOKENS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function addToken(token) {
		var postData = 'token=' + token.token + '&period=' + token.period + '&userId=' + token.userId;
		return $http.post(
			TOKENS_URL,
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
			TOKENS_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}	

	function deleteToken(token) {
		return $http.delete(TOKENS_URL + token);
	}

	function getUsers() {
		return $http.get(USERS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function addUser(postData) {
		return $http.post(
			USERS_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteUser(userId) {
		return $http.delete(USERS_URL + userId);
	}

	function getRoles() {
		return $http.get(ROLES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function addRole(postData) {
		return $http.post(
			ROLES_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteRole(roleId) {
		return $http.delete(ROLES_URL + roleId);
	}

	function getTokenTypes() {
		return $http.get(USERS_URL + 'tokens/types', {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getPermissionsScops() {
		return $http.get(USERS_URL + 'permissions/scopes', {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getCurrentUser() {
		return $http.get (SERVER_URL + 'rest/ui/identity/user');
	}

}
