'use strict';

angular.module('subutai.kurjun.controller', [])
	.controller('KurjunViewCtrl', KurjunViewCtrl)
	.directive('fileModel', fileModel);

KurjunViewCtrl.$inject = ['$scope', '$rootScope', 'kurjunSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', '$resource', '$compile', 'ngDialog', '$timeout', 'cfpLoadingBar'];
fileModel.$inject = ['$parse'];

var fileUploder = {};

function KurjunViewCtrl($scope, $rootScope, kurjunSrv, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, $resource, $compile, ngDialog, $timeout, cfpLoadingBar) {

	var vm = this;
	vm.activeTab = 'templates';
	vm.repositories = [];
	vm.templates = [];
	vm.apts = [];

	vm.openTab = openTab;
	vm.addTemplate = addTemplate;
	vm.proceedTemplate = proceedTemplate;
	vm.deleteTemplate = deleteTemplate;
	vm.deleteAPT = deleteAPT;

	/*** Get templates according to repositories ***/
	function getTemplates() {
		kurjunSrv.getRepositories().success(function (repositories) {
			vm.repositories = repositories;
			vm.templates = [];
			var getTemplatesRecursively = function (index, repositories) {
				kurjunSrv.getTemplates(repositories[index]).then(function (templates) {
					for (var template in templates.data) {
						templates.data[template].repository = repositories[index];
						vm.templates.push(templates.data[template]);
					}
					if (( index + 1 ) < repositories.length) {
						getTemplatesRecursively(index + 1, repositories);
					} else {
						return;
					}
				});
			};
			getTemplatesRecursively(0, repositories);
		});
	}
	getTemplates();

	/*** Get all APTs ***/
	function getAPTs() {
		kurjunSrv.getAPTList().success(function (aptList) {
			vm.aptList = aptList;
		});
	}
	getAPTs();

	function openTab(tab) {
		vm.dtOptions = DTOptionsBuilder
				.newOptions()
				.withOption('order', [[ 0, "desc" ]])
				.withOption('stateSave', true)
				.withPaginationType('full_numbers');

		switch (tab) {
			case 'templates':
				vm.dtColumnDefs = [
					DTColumnDefBuilder.newColumnDef(0),
					DTColumnDefBuilder.newColumnDef(1),
					DTColumnDefBuilder.newColumnDef(2).notSortable(),
				];
				break;
			case 'apt':
				vm.dtColumnDefs = [
					DTColumnDefBuilder.newColumnDef(0),
					DTColumnDefBuilder.newColumnDef(1)
				];
				break;
			default:
				break;
		}
		vm.activeTab = tab;
	}

	function addTemplate(repository) {
		switch (vm.activeTab) {
			case 'templates':
				vm.currentTemplate = {file: null};
				ngDialog.open({
					template: 'subutai-app/kurjun/partials/template-form.html',
					scope: $scope
				});
				break;
			case 'apt':
				vm.currentTemplate = {repository: null,file: null};
				ngDialog.open({
					template: 'subutai-app/kurjun/partials/apt-form.html',
					scope: $scope
				});
				break;
			default:
				break;
		}
	}

	function proceedTemplate(template) {
		switch (vm.activeTab) {
			case 'templates':
				kurjunSrv.addTemplate(template.repository, template.file).then(function (response) {
					$timeout(function () {
						template.file.result = response.data;
						LOADING_SCREEN('none');
						SweetAlert.swal("Success!", "You have successfully uploaded template", "success");
						getTemplates();
					}, 2000);
				}, function (response) {
					if (response.status > 0) {
						$timeout(function () {
							ngDialog.closeAll();
							LOADING_SCREEN('none');
							SweetAlert.swal("ERROR!", "Your template is safe. Error: " + response.data, "error");
						}, 2000);
					}
				}, function (event) {
					template.file.progress = Math.min(100, parseInt(100.0 * event.loaded / event.total));
					if(template.file.progress == 100) {
						$timeout(function () {
							ngDialog.closeAll();
							LOADING_SCREEN();
						}, 1000);
					}
				});
				break;
			case 'apt':
				kurjunSrv.addApt(template.file).then(function (response) {
					template.file.result = response.data;
					$timeout(function () {
						LOADING_SCREEN('none');
						SweetAlert.swal("Success!", "You have successfully uploaded APT", "success");
						getAPTs();
					}, 2000);
				}, function (response) {
					if (response.status > 0) {
						$timeout(function () {
							ngDialog.closeAll();
							LOADING_SCREEN('none');
							SweetAlert.swal("ERROR!", "Your APT is safe. Error: " + response.data, "error");
						}, 2000);
					}
				}, function (event) {
					template.file.progress = Math.min(100, parseInt(100.0 * event.loaded / event.total));
					if (template.file.progress == 100) {
						$timeout(function () {
							ngDialog.closeAll();
							LOADING_SCREEN();
						}, 1000);
					}
				});
				break;
			default:
				break;
		}
	}

	function deleteTemplate(template) {
		SweetAlert.swal({
					title: "Are you sure?",
					text: "Delete template!",
					type: "warning",
					showCancelButton: true,
					confirmButtonColor: "#ff3f3c",
					confirmButtonText: "Delete",
					cancelButtonText: "Cancel",
					closeOnConfirm: false,
					closeOnCancel: true,
					showLoaderOnConfirm: true
				},
				function (isConfirm) {
					if (isConfirm) {
						LOADING_SCREEN();
						kurjunSrv.deleteTemplate(template.md5Sum, template.repository).success(function (data) {
							LOADING_SCREEN('none');
							SweetAlert.swal("Deleted!", "Your template has been deleted.", "success");
							getTemplates();
						}).error(function (data) {
							LOADING_SCREEN('none');
							SweetAlert.swal("ERROR!", "Your template is safe. Error: " + data, "error");
						});
					}
				});
	}

	function deleteAPT(apt) {
		SweetAlert.swal({
					title: "Are you sure?",
					text: "Delete template!",
					type: "warning",
					showCancelButton: true,
					confirmButtonColor: "#ff3f3c",
					confirmButtonText: "Delete",
					cancelButtonText: "Cancel",
					closeOnConfirm: false,
					closeOnCancel: true,
					showLoaderOnConfirm: true
				},
				function (isConfirm) {
					if (isConfirm) {
						LOADING_SCREEN();
						kurjunSrv.deleteAPT(apt.md5Sum).success(function (data) {
							LOADING_SCREEN('none');
							SweetAlert.swal("Deleted!", "Your APT has been deleted.", "success");
							getAPTs();
						}).error(function (data) {
							LOADING_SCREEN('none');
							SweetAlert.swal("ERROR!", "Your template is safe. Error: REST Endpoint is under modification", "error");
						});
					}
				});
	}

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		$timeout(function () {
			cfpLoadingBar.complete();
		}, 500);
	});
}

function fileModel($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function(){
				document.getElementById("js-uploadFile").value = element[0].files[0].name;
				scope.$apply(function(){
					modelSetter(scope, element[0].files[0]);
					fileUploder = element[0].files[0];
				});
			});
		}
	};
}

