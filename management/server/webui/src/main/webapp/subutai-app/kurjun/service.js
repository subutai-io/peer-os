'use strict';

angular.module('subutai.kurjun.service', [])
	.factory('kurjunSrv', kurjunService);


kurjunService.$inject = ['$http', 'Upload'];

function kurjunService($http, Upload) {

	var KURJUN_URL = SERVER_URL + 'rest/kurjun/';
	var REPOSITORIES_URL = KURJUN_URL + 'templates/repositories';

	var kurjunService = {
		getRepositories: getRepositories,
		getTemplates: getTemplates,
		getAPTList: getAPTList,
		addTemplate: addTemplate,
		addApt: addApt,
		deleteTemplate: deleteTemplate,
		deleteAPT: deleteAPT,
		isUploadAllowed: isUploadAllowed
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

	function addTemplate(repository, file) {
		return uploadFile(file, KURJUN_URL + 'templates/upload/' + repository);
	}

	function addApt(file) {
		return uploadFile(file, KURJUN_URL + 'vapt/upload');
	}

	function deleteTemplate(md5, repository) {
		return $http.delete(KURJUN_URL + 'templates/' + repository, {params: {md5: md5}}, {withCredentials: true,headers: {'Content-Type': 'application/json'}});
	}

	function deleteAPT(md5) {
		return $http.delete(KURJUN_URL + 'apt/delete', {params: {md5: md5}}, {withCredentials: true,headers: {'Content-Type': 'application/json'}});
	}

	function isUploadAllowed(repository) {
		return $http.get(KURJUN_URL + 'templates/' + repository + '/can-upload', {withCredentials: true,headers: {'Content-Type': 'application/json'}});
	}

	function uploadFile(file, url) {
		return Upload.upload({
			url: url,
			data: {'package': file},
			headers: {'Content-Type': undefined},
			transformRequest: angular.identity
		});
	}
}
