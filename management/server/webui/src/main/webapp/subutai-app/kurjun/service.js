'use strict';

angular.module('subutai.kurjun.service', [])
	.factory('kurjunSrv', kurjunService);


kurjunService.$inject = ['$http', 'Upload', 'SettingsKurjunSrv'];

function kurjunService($http, Upload, SettingsKurjunSrv)
{
	var BASE_URL = GLOBAL_KURJUN_URL + "/";
	var TEMPLATE_URL = BASE_URL + "template/";
	var DEB_URL = BASE_URL + "deb/";
	var RAW_URL = BASE_URL + "file/";

	var LOCAL_BASE_URL = SERVER_URL + "rest/kurjun/";
	var LOCAL_TEMPLATE_URL = LOCAL_BASE_URL + "template/";
	var LOCAL_DEB_URL = LOCAL_BASE_URL + "deb/";
	var LOCAL_RAW_URL = LOCAL_BASE_URL + "file/";

	var REPOSITORY_URL = LOCAL_BASE_URL + "templates/repositories";


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
		addFile:addFile,
		uploadFile: uploadFile
	};

	return kurjunService;

	function setUrlsValues() {
		BASE_URL = GLOBAL_KURJUN_URL + "/";
		TEMPLATE_URL = BASE_URL + "template/";
		// REPOSITORY_URL = BASE_URL + "repository/";
		DEB_URL = BASE_URL + "deb/";
		RAW_URL = BASE_URL + "file/";
	}

	function getRepositories() {
		setUrlsValues();
		return $http.get(REPOSITORY_URL, {withCredentials: false, headers: {'Content-Type': 'application/json'}});
	}

	function getTemplates(repository) {
		setUrlsValues();
		return $http.get(LOCAL_TEMPLATE_URL + 'list?repository=all', {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function getAPTList() {
		setUrlsValues();
		return $http.get(DEB_URL + "list?repository=all", {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function getRawFiles() {
		setUrlsValues();
		return $http.get(LOCAL_RAW_URL + "list?repository=all", {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function addFile(file) {
		return uploadFile(file, LOCAL_RAW_URL + 'upload');
	}

	function addTemplate(file) {
		//setUrlsValues();
		//@todo repository=
		return uploadTemplate(file, LOCAL_TEMPLATE_URL + 'upload');
	}

	function addApt(file) {
		return uploadApt(file, LOCAL_DEB_URL + 'upload');
	}

	function deleteTemplate(id) {
		setUrlsValues();
		return $http.delete(LOCAL_TEMPLATE_URL + 'delete', {params: {id: id}}, {
			withCredentials: false,
			headers: {'Content-Type': 'application/json'}
		});
	}

	function shareTemplate(users, templateId) {
		setUrlsValues();
		// TODO: doesn't work properly
		var postData = "users=" + users + "&templateId=" + templateId;
		return $http.post(
			BASE_URL + "share",
			postData,
			{withCredentials: false, headers: {'Content-Type': 'application/x-www-form-urlencoded'}}
		);
	}

	function deleteAPT(md5) {
		setUrlsValues();
		return $http.delete(LOCAL_DEB_URL + 'delete', {params: {md5: md5}}, {
			withCredentials: false,
			headers: {withCredentials: false, 'Content-Type': 'application/json'}
		});
	}

	function isUploadAllowed(repository) {
		setUrlsValues();
		return $http.get(LOCAL_TEMPLATE_URL + repository + '/can-upload', {
			withCredentials: false,
			headers: {withCredentials: false, 'Content-Type': 'application/json'}
		});
	}

	function getShared(templateId) {
		// TODO: doesn't work properly
		return $http.get (BASE_URL + "shared/users/" + templateId);
	}

	function uploadTemplate(file, url) {
		return Upload.upload({
			url: url,
			data: {package: file},
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
