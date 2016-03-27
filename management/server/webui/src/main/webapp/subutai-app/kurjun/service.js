'use strict';

angular.module('subutai.kurjun.service', [])
	.factory('kurjunSrv', kurjunService);


kurjunService.$inject = ['$http', 'Upload'];

function kurjunService($http, Upload) {

	var KURJUN_URL = SERVER_URL + 'rest/kurjun/';
	var REPOSITORIES_URL = KURJUN_URL + 'templates/repositories';

	var GLOBAL_KURJUN = "https://peer.noip.me:8339/kurjun"
	var baseUrl = GLOBAL_KURJUN + "/rest/";
	var baseTemplateUrl = baseUrl + "template/";


	var kurjunService = {
		getRepositories: getRepositories,
		getTemplates: getTemplates,
		getAPTList: getAPTList,
		addTemplate: addTemplate,
		shareTemplate: shareTemplate,
		addApt: addApt,
		deleteTemplate: deleteTemplate,
		deleteAPT: deleteAPT,
		isUploadAllowed: isUploadAllowed,
		getShared: getShared
	};

	return kurjunService;

	function getRepositories() {
		return $http.get(REPOSITORIES_URL, {withCredentials: true, headers: {'Content-Type': 'application/json'}});
	}

	function getTemplates(repository) {
		return $http.get(baseTemplateUrl +'list', {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function getAPTList() {
		return $http.get(KURJUN_URL + 'vapt/list', {
			withCredentials: true,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function addTemplate(repository, file) {
		return uploadFile(file, KURJUN_URL + 'templates/upload/' + repository);
	}

	function addApt(file) {
		return uploadFile(file, KURJUN_URL + 'vapt/upload');
	}

	function deleteTemplate(md5, repository) {
		return $http.delete(KURJUN_URL + 'templates/' + repository, {params: {md5: md5}}, {
			withCredentials: true,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function shareTemplate(users, templateId) {
		var postData = "users=" + users + "&templateId=" + templateId;
		return $http.post(
			KURJUN_URL + "share",
			postData,
			{withCredentials: true, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteAPT(md5) {
		return $http.delete(KURJUN_URL + 'vapt/delete', {params: {md5: md5}}, {
			withCredentials: true,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function isUploadAllowed(repository) {
		return $http.get(KURJUN_URL + 'templates/' + repository + '/can-upload', {
			withCredentials: true,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function getShared(templateId) {
		return $http.get (KURJUN_URL + "shared/users/" + templateId);
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
