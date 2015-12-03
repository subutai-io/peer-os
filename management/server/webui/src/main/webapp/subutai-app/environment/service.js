'use strict';

angular.module('subutai.environment.service', [])
	.factory('environmentService', environmentService);


environmentService.$inject = ['$http'];

function environmentService($http) {

	var ENVIRONMENTS_URL = SERVER_URL + 'rest/ui/environments/';

	var SSH_KEY_URL = ENVIRONMENTS_URL + 'keys/';
	var CONTAINERS_URL = ENVIRONMENTS_URL + 'containers/';
	var CONTAINER_TYPES_URL = CONTAINERS_URL + 'types/';
	var DOMAINS_URL = ENVIRONMENTS_URL + 'domains/';

	var BLUEPRINT_URL = ENVIRONMENTS_URL + 'blueprints/';

	var GROW_BLUEPRINT_URL = ENVIRONMENTS_URL + 'grow/';

	var STRATEGIES_URL = ENVIRONMENTS_URL + 'strategies/';

	var TEMPLATES_URL = ENVIRONMENTS_URL + 'templates/';

	var PEERS_URL = ENVIRONMENTS_URL + 'peers/';


	var environmentService = {
		getTemplates: getTemplates,


		getBlueprints: getBlueprints,
		getBlueprintById: getBlueprintById,
		saveBlueprint : saveBlueprint,
		deleteBlueprint : deleteBlueprint,
		getStrategies : getStrategies,


		getEnvironments : getEnvironments,
		createEnvironment : createEnvironment,
		growEnvironment : growEnvironment,
		destroyEnvironment: destroyEnvironment,


		setSshKey : setSshKey,
		removeSshKey : removeSshKey,


		getDomainStrategies : getDomainStrategies,
		getDomain : getDomain,
		setDomain : setDomain,
		removeDomain : removeDomain,


		getContainerStatus : getContainerStatus,
		destroyContainer : destroyContainer,
		switchContainer : switchContainer,
		getContainerDomain : getContainerDomain,
		checkDomain : checkDomain,


		getContainersType : getContainersType,
		setTags : setTags,
		removeTag : removeTag,


		getEnvQuota: getEnvQuota,
		updateQuota: updateQuota,



		getPeers : getPeers,


		getServerUrl : function getServerUrl() { return ENVIRONMENTS_URL; }
	};

	return environmentService;




	//// Implementation

	function getTemplates() {
		return $http.get(TEMPLATES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getBlueprints() {
		return $http.get(BLUEPRINT_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getBlueprintById(blueprintId) {
		return $http.get(BLUEPRINT_URL + blueprintId, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function saveBlueprint(blueprint_json) {
		var data = 'blueprint_json=' + blueprint_json;
		return $http.post(
			BLUEPRINT_URL,
			data,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteBlueprint(blueprintId) {
		return $http.delete(BLUEPRINT_URL + blueprintId);
	}



	function getEnvironments() {
		return $http.get(ENVIRONMENTS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function createEnvironment(data) {
		var postData = 'blueprint_json=' + data;
		return $http.post(
			ENVIRONMENTS_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function growEnvironment(environmentId, data) {
		var postData = 'environmentId=' + environmentId + '&blueprint_json=' + data;
		return $http.post(
			GROW_BLUEPRINT_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function destroyEnvironment(environmentId) {
		return $http.delete(ENVIRONMENTS_URL + environmentId);
	}



	function switchContainer(containerId, type) {
		return $http.post(
			CONTAINERS_URL + containerId + '/' + type,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function getContainerStatus(containerId) {
		return $http.get(
			CONTAINERS_URL + containerId + '/state',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function destroyContainer(containerId) {
		return $http.delete(CONTAINERS_URL + containerId);
	}


	function setSshKey(sshKey, environmentId) {
		var postData = 'environmentId=' + environmentId + '&key=' + window.btoa(sshKey);
		return $http.post(
			SSH_KEY_URL,
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function removeSshKey(environmentId) {
		return $http.delete(ENVIRONMENTS_URL + environmentId + '/keys');
	}


	function getDomain(environmentId) {
		return $http.get(
			ENVIRONMENTS_URL + environmentId + '/domain',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function getContainerDomain(container) {
		return $http.get(
			ENVIRONMENTS_URL + container.environmentId + '/containers/' + container.id + '/domain',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}

	function checkDomain(container) {
		return $http.put(ENVIRONMENTS_URL + container.environmentId + '/containers/' + container.id + '/domain');
	}


	function getDomainStrategies() {
		return $http.get(
			DOMAINS_URL + 'strategies/',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}


	function setDomain(domain, envId, file) {
		var fd = new FormData();
		fd.append('environmentId', envId);
		fd.append('hostName', domain.name);
		fd.append('strategy', domain.strategy);
		fd.append('file', file);

		return $http.post(
			ENVIRONMENTS_URL + 'domains',
			fd,
			{transformRequest: angular.identity, headers: {'Content-Type': undefined}}
		);
	}

	function removeDomain( envId ) {
		return $http.delete( ENVIRONMENTS_URL + envId + '/domains' );
	}


	function getContainersType() {
		return $http.get(CONTAINER_TYPES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getEnvQuota(containerId) {
		return $http.get(
			CONTAINERS_URL + containerId + '/quota',
			{withCredentials: true, headers: {'Content-Type': 'application/json'}}
		);
	}	

	function updateQuota(containerId, postData) {
		return $http.post(
			CONTAINERS_URL + containerId + '/quota',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}


	function getStrategies() {
		return $http.get(STRATEGIES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}


	function getPeers() {
		return $http.get(PEERS_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function setTags(environmentId, containerId, tags) {
		var postData = 'tags=' + JSON.stringify(tags);
		return $http.post(
			ENVIRONMENTS_URL + environmentId + '/containers/' + containerId + '/tags',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function removeTag(environmentId, containerId, tag) {
		return $http.delete(ENVIRONMENTS_URL + environmentId + '/containers/' + containerId + '/tags/' + tag);		
	}
}
