'use strict';

angular.module('subutai.kurjun.service', [])
	.factory('kurjunSrv', kurjunService);


kurjunService.$inject = ['$http'];

function kurjunService($http) {

	var KURJUN_URL = SERVER_URL + 'rest/kurjun/';
	var REPOSITORIES_URL = KURJUN_URL + 'templates/repositories';

	var kurjunService = {
		getRepositories: getRepositories,
		getTemplates: getTemplates,
		getAPTList: getAPTList,
		deleteTemplate: deleteTemplate,
		addTemplate: addTemplate,
		addAptTemplate: addAptTemplate
	};

	return kurjunService;

	function getRepositories() {
		return $http.get(REPOSITORIES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getTemplates(repository) {
		return $http.get(KURJUN_URL + 'templates/' + repository + '/template-list', {withCredentials: true,headers: {'Content-Type': 'application/json'}});
	}

	function getAPTList() {
		return $http.get(KURJUN_URL + 'vapt/list', {withCredentials: true,headers: {'Content-Type': 'application/json'}});
	}

	function deleteTemplate(md5, repository) {
		return $http.delete(KURJUN_URL + 'templates/' + repository, {params: {md5: md5}}, {withCredentials: true,headers: {'Content-Type': 'application/json'}});
	}

	function addTemplate(repository, file) {
		var fd = new FormData();
		fd.append('package', file);

		return $http.post(
				KURJUN_URL + 'templates/upload/' + repository,
				fd,
				{transformRequest: angular.identity, headers: {'Content-Type': undefined}}
		);
	}

	function addAptTemplate(file) {
		var fd = new FormData();
		fd.append('package', file);

		return $http.post(
				KURJUN_URL + 'vapt/upload/' ,
				fd,
				{transformRequest: angular.identity, headers: {'Content-Type': undefined}}
		);
	}
}
