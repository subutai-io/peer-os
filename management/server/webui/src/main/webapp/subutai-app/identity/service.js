'use strict';

angular.module('subutai.identity.service', [])
	.factory('identitySrv', identitySrv);


identitySrv.$inject = ['$http'];

function identitySrv($http) {
	var BASE_URL = serverUrl + 'identity_ui/';
	var usersURL = BASE_URL;
	var rolesURL = BASE_URL + 'roles/';

	var identitySrv = {
		getUsers: getUsers,
		addUser : addUser,
		deleteUser: deleteUser,
		getRoles: getRoles,
		addRole: addRole,
		deleteRole: deleteRole
	};

	return identitySrv;

	//// Implementation

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
