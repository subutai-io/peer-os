'use strict';

angular.module('subutai.environment.service', [])
	.factory('environmentService', environmentService);


environmentService.$inject = ['$http', '$rootScope'];

function environmentService($http, $rootScope) {
	var BASE_URL = serverUrl + 'environments_ui/';
	var blueprintURL = BASE_URL + 'blueprints/';
	var growBlueprintURL = BASE_URL + 'grow/';
	var templatesURL = BASE_URL + 'templates/';
	var peersURL = BASE_URL + 'peers/';
	var strategiesURL = BASE_URL + 'strategies/';
	var environmentsURL = BASE_URL;
	var sshKeysURL = environmentsURL + 'key/';
	var containersURL = environmentsURL + 'containers/';
	var containersTypeURL = containersURL + 'types/';

	var environmentService = {
		getBlueprints: getBlueprints,
		getBlueprintById: getBlueprintById,
		createBlueprint : createBlueprint,
		getTemplates: getTemplates,
		deleteBlueprint : deleteBlueprint,
		getPeers : getPeers,
		getStrategies : getStrategies,
		buildBlueprint : buildBlueprint,
		growBlueprint : growBlueprint,
		getContainersType : getContainersType,

		getEnvironments : getEnvironments,
		destroyEnvironment: destroyEnvironment,
		getContainerStatus : getContainerStatus,
		switchContainer : switchContainer,
		destroyContainer : destroyContainer,
		addSshKey : addSshKey,
		removeSshKey : removeSshKey,

		getEnvQuota: getEnvQuota,
		updateQuota: updateQuota
	};

	return environmentService;

	//// Implementation

	function getBlueprints() {
		return $http.get(blueprintURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getBlueprintById(blueprintId) {
		return $http.get(blueprintURL + blueprintId, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getTemplates() {
		return $http.get(templatesURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getContainersType() {
		return $http.get(containersTypeURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function createBlueprint(blueprint_json) {
		var data = 'blueprint_json=' + blueprint_json;
		return $http.post(
			blueprintURL, 
			data, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteBlueprint(blueprintId) {
		return $http.delete(blueprintURL + blueprintId);
	}

	function getPeers() {
		return $http.get(peersURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getStrategies() {
		return $http.get(strategiesURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function buildBlueprint(data) {
		var postData = 'blueprint_json=' + data;
		return $http.post(
			environmentsURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function growBlueprint(environmentId, data) {
		var postData = 'environmentId=' + environmentId + '&blueprint_json=' + data;
		return $http.post(
			growBlueprintURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function getEnvironments() {
		return $http.get(environmentsURL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function destroyEnvironment(environmentId) {
		return $http.delete(environmentsURL + environmentId);		
	}

	function getContainerStatus(containerId) {
		return $http.get(
			containersURL + containerId + '/state', 
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function switchContainer(containerId, type) {
		return $http.post(
			containersURL + containerId + '/' + type, 
			'', 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function addSshKey(sshKey, environmentId) {
		var postData = 'environmentId=' + environmentId + '&key=' + sshKey;
		console.log(postData);
		return $http.post(
			sshKeysURL, 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function destroyContainer(containerId) {
		return $http.delete(containersURL + containerId);		
	}

	function removeSshKey(environmentId) {
		return $http.delete(environmentsURL + environmentId + '/keys');		
	}

	function getEnvQuota(containerId) {
		return $http.get(
			containersURL + containerId + '/quota', 
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}	

	function updateQuota(containerId, postData) {
		return $http.post(
			containersURL + containerId + '/quota', 
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

}
