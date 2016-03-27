'use strict';

angular.module('subutai.kurjun.service', [])
	.factory('kurjunSrv', kurjunService);


kurjunService.$inject = ['$http', 'Upload', 'SettingsKurjunSrv'];

function kurjunService($http, Upload, SettingsKurjunSrv) {

	/*var KURJUN_URL = SERVER_URL + 'rest/kurjun/';
	var REPOSITORIES_URL = KURJUN_URL + 'templates/repositories';*/

	var GLOBAL_KURJUN_URL = "https://peer.noip.me:8339/kurjun/";
	var BASE_URL = GLOBAL_KURJUN_URL + "rest/";
	var TEMPLATE_URL = BASE_URL + "template/";
	var REPOSITORY_URL = BASE_URL + "repository/";
	var DEB_URL = BASE_URL + "deb/";
	var RAW_URL = BASE_URL + "file/";

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
		getShared: getShared,
		getRawFiles: getRawFiles,
		uploadFile: uploadFile
	};

	return kurjunService;

	SettingsKurjunSrv.getConfig().success (function (data) {
		GLOBAL_KURJUN_URL = data.globalKurjunUrls[0];
	});

	function getRepositories() {
		return $http.get(REPOSITORY_URL + "list", {withCredentials: false, headers: {'Content-Type': 'application/json'}});
	}

	function getTemplates(repository) {
		return $http.get(TEMPLATE_URL + 'list', {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function getAPTList() {
		return $http.get(DEB_URL + "list", {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function getRawFiles() {
		return $http.get(RAW_URL + "list", {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function uploadFile(file) {
		return uploadFile(file, RAW_URL + 'upload');
	}

	function addTemplate(repository, file) {
		return uploadTemplate(file, TEMPLATE_URL + 'upload', repository);
	}

	function addApt(file) {
		return uploadApt(file, DEB_URL + 'upload');
	}

	function deleteTemplate(id) {
		return $http.delete(TEMPLATE_URL + 'delete', {params: {id: id}}, {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function shareTemplate(users, templateId) {
		// TODO: doesn't work properly
		var postData = "users=" + users + "&templateId=" + templateId;
		return $http.post(
			KURJUN_URL + "share",
			postData,
			{withCredentials: false, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteAPT(md5) {
		return $http.delete(DEB_URL + 'delete', {params: {md5: md5}}, {
			withCredentials: false,
			headers: {withCredentials: false, 'Content-Type': 'application/json'}
		});
	}

	function isUploadAllowed(repository) {
		// TODO: no alternative for global kurjun
		return $http.get(KURJUN_URL + 'templates/' + repository + '/can-upload', {
			withCredentials: false,
			headers: {withCredentials: false, 'Content-Type': 'application/json'}
		});
	}

	function getShared(templateId) {
		// TODO: doesn't work properly
		return $http.get (KURJUN_URL + "shared/users/" + templateId);
	}

	function uploadTemplate(file, url, repository) {
		return Upload.upload({
			url: url,
			data: {file: file, repository: repository},
			headers: {withCredentials: false, 'Content-Type': undefined},
			transformRequest: angular.identity
		});
	}

	function uploadApt(file, url) {
		return Upload.upload({
			url: url,
			data: {file: file},
			headers: {withCredentials: false, 'Content-Type': undefined},
			transformRequest: angular.identity
		});
	}

	function uploadFile(file, url) {
		return Upload.upload({
			url: url,
			data: {file: file},
			headers: {withCredentials: false, 'Content-Type': undefined},
			transformRequest: angular.identity
		});
	}
}
