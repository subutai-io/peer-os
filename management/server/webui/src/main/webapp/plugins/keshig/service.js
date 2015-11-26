'use strict';

angular.module('subutai.plugins.keshig.service', [])
	.factory('keshigSrv', keshigSrv);

keshigSrv.$inject = ['$http'];

function keshigSrv($http) {

	var BASE_URL = SERVER_URL + 'rest/v1/keshig/';
	var SERVERS_URL = BASE_URL + 'server/';
	var OPTIONS_URL = BASE_URL + 'option/';
	var PROFILES_URL = BASE_URL + 'profiles/';
	var HISTORY_URL = BASE_URL + 'history/';


	var keshigSrv = {
		getProfiles : getProfiles,
		addProfile : addProfile,
		updateProfile : updateProfile,
		removeProfile : removeProfile,
		startProfile : startProfile,
		getServers : getServers,
		addServer : addServer,
		removeServer : removeServer,
		updateServer : updateServer,
		getServerTypes : getServerTypes,
		getAllOptions : getAllOptions,
		getOptionTypes : getOptionTypes,
		getOptionsByType : getOptionsByType,
		startOption : startOption,
		addOption : addOption,
		deleteOption : deleteOption,
		getBuilds: getBuilds,
		getPlaybooks: getPlaybooks,
		updateOption : updateOption,


		getServersUrl : function(){ return SERVERS_URL; },
		getOptionsUrl : function(){ return OPTIONS_URL; },
		getProfilesUrl : function(){ return PROFILES_URL },
		getHistoryUrl : function(){ return HISTORY_URL }
	};

	return keshigSrv;

	/*
	 *   Keshig Server Services
	 * */

	function getBuilds() {
		return $http.get(BASE_URL + 'build', {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getPlaybooks() {
		return $http.get(BASE_URL + 'tests', {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getProfiles() {
		return $http.get(PROFILES_URL, {
			withCredentials: true
		});
	}

	function startProfile(profileName) {
		return $http.get(PROFILES_URL + profileName + '/start', {
			withCredentials: true
		});
	}

	function addProfile( profile ) {
		return $http.post(PROFILES_URL, profile, {
			withCredentials: true,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function updateProfile( profile ) {
		return $http.put(PROFILES_URL, profile, {
			withCredentials: true,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function removeProfile( name ) {
		return $http.delete(PROFILES_URL + name, {
			withCredentials: true
		});
	}

	function getServers() {
		return $http.get(SERVERS_URL, {
			withCredentials: true
		});
	}

	function addServer( server ) {
		var postData = 'serverName=' + server.serverName 
			+ '&serverType=' + server.serverType 
			+ '&serverId=' + server.serverId;
		return $http.post(SERVERS_URL, postData, {
			withCredentials: true,
			headers: {'Content-Type': 'application/x-www-form-urlencoded'}
		});
	}

	function removeServer(hostId) {
		return $http.delete(SERVERS_URL + hostId, {
			withCredentials: true
		});
	}


	function getServerTypes() {
		return $http.get(SERVERS_URL + 'types', {
			withCredentials: true
		});
	}

	function updateServer( server ) {
		var postData = 'serverName=' + server.serverName 
			+ '&serverType=' + server.serverType 
			+ '&serverId=' + server.serverId;		
		return $http.put(SERVERS_URL, postData, {
			withCredentials: true,
			headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
		});
	}

	function getAllOptions() {
		return $http.get(OPTIONS_URL, {
			withCredentials: true
		});
	}

	function getOptionTypes() {
		return $http.get(OPTIONS_URL + 'types', {
			withCredentials: true
		});
	}

	function getOptionsByType(type) {
		return $http.get(OPTIONS_URL + 'type/' + type.toLowerCase(), {
			withCredentials: true
		});
	}

	function deleteOption(type, optionName) {
		return $http.delete(OPTIONS_URL + type.toLowerCase() + '/' + optionName, {
			withCredentials: true
		});
	}

	function startOption(type, optionName) {
		return $http.get(OPTIONS_URL + type.toLowerCase() + '/' + optionName + '/' + 'start', {
			withCredentials: true
		});
	}

	function addOption( type, object ) {
		return $http.post(OPTIONS_URL + type.toLowerCase(), object, {
			withCredentials: true,
			headers: { 'Content-Type': 'application/json' }
		});
	}

	function updateOption( type, object ) {
		return $http.put(OPTIONS_URL + type.toLowerCase(), object, {
			withCredentials: true,
			headers: { 'Content-Type': 'application/json' }
		});
	}
}
