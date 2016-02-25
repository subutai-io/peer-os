'use strict';

angular.module('subutai.environment.service', [])
	.factory('environmentService', environmentService);


environmentService.$inject = ['$http'];

function environmentService($http) {

	var ENVIRONMENTS_URL = SERVER_URL + 'rest/ui/environments/';

	var ENVIRONMENT_START_BUILD = ENVIRONMENTS_URL + 'build/';
	var ENVIRONMENT_ADVANCED_BUILD = ENVIRONMENTS_URL + 'build/advanced';

	var SSH_KEY_URL = ENVIRONMENTS_URL + 'keys/';
	var CONTAINERS_URL = ENVIRONMENTS_URL + 'containers/';
	var CONTAINER_TYPES_URL = CONTAINERS_URL + 'types/';
	var DOMAINS_URL = ENVIRONMENTS_URL + 'domains/';

	var BLUEPRINT_URL = ENVIRONMENTS_URL + 'blueprints/';

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
		startEnvironmentAdvancedBuild : startEnvironmentAdvancedBuild,
		startEnvironmentAutoBuild: startEnvironmentAutoBuild,
		destroyEnvironment: destroyEnvironment,
		modifyEnvironment: modifyEnvironment,


		setSshKey : setSshKey,
		getSshKey : getSshKey,
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


		getShared: getShared,
		share: share,

		revoke: revoke,

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


	function startEnvironmentAutoBuild(environmentName, containers) {
		var postData = 'name=' + environmentName + "&topology=" + containers;
		return $http.post(
			ENVIRONMENT_START_BUILD,
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function startEnvironmentAdvancedBuild(environmentName, containers) {
		var postData = 'name=' + environmentName + "&topology=" + containers;
		return $http.post(
			ENVIRONMENT_ADVANCED_BUILD,
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function destroyEnvironment(environmentId) {
		return $http.delete(ENVIRONMENTS_URL + environmentId);
	}

	function modifyEnvironment(containers, advanced) {
		if(advanced == undefined || advanced == null) advanced = '';
		var postData = 'topology=' + JSON.stringify( containers.topology ) + '&removedContainers=' + JSON.stringify( containers.removedContainers );
		return $http.post(
			ENVIRONMENTS_URL + containers.environmentId + '/modify/' + advanced,
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function switchContainer(containerId, type) {
		return $http.put(
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
		var postData = 'key=' + window.btoa(sshKey);
		return $http.post(
			ENVIRONMENTS_URL + environmentId + '/keys',
			postData, 
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function getSshKey(environmentId) {
		return $http.get(ENVIRONMENTS_URL + environmentId + '/keys');
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
		fd.append('hostName', domain.name);
		fd.append('strategy', domain.strategy);
		fd.append('file', file);

		return $http.post(
			ENVIRONMENTS_URL + envId + '/domains',
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


	function getShared (environmentId) {
		return $http.get (ENVIRONMENTS_URL + "shared/users/" + environmentId);
	}

	function share (users, environmentId) {
		var postData = "users=" + users;
		return $http.post(
			ENVIRONMENTS_URL + environmentId + "/share",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function revoke (environmentId) {
		return $http.put (ENVIRONMENTS_URL + environmentId + "/revoke");
	}
}
