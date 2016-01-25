'use strict';

angular.module('subutai.kurjun.controller', [])
	.controller('KurjunViewCtrl', KurjunViewCtrl)
	.directive('fileModel', fileModel);

KurjunViewCtrl.$inject = ['$scope', '$rootScope', 'kurjunSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', '$resource', '$compile', 'ngDialog', '$timeout', 'cfpLoadingBar'];
fileModel.$inject = ['$parse'];

var fileUploder = {};

function KurjunViewCtrl($scope, $rootScope, kurjunSrv, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, $resource, $compile, ngDialog, $timeout, cfpLoadingBar) {

	var vm = this;
	vm.activeTab = 'subutai-templates';
	vm.repositories = [];
	vm.templates = [];
	vm.apts = [];

	vm.deleteTemplate = deleteTemplate;
	vm.addSubutaiTemplateForm = addSubutaiTemplateForm;
	vm.proceedTemplate = proceedTemplate;
	vm.addTemplate = addTemplate;
	vm.addAPTForm = addAPTForm;
	vm.addAptTemplate = addAptTemplate;
	vm.openTab = openTab;

	kurjunSrv.getRepositories().success(function (repositories) {
		vm.repositories = repositories;
		vm.templates = [];
		getTemplates(0, repositories);
	});

	kurjunSrv.getAPTList().success(function (aptList) {
		vm.aptList = aptList;
	});

	function openTab(tab) {
		if(tab == 'apt-templates' ) {
			vm.dtOptions = DTOptionsBuilder
					.newOptions()
					.withOption('order', [[ 0, "desc" ]])
					.withOption('stateSave', true)
					.withPaginationType('full_numbers');
			vm.dtColumnDefs = [
				DTColumnDefBuilder.newColumnDef(0),
				DTColumnDefBuilder.newColumnDef(1)
			];
		}
		if(tab == 'subutai-templates') {
			vm.dtOptions = DTOptionsBuilder
					.newOptions()
					.withOption('order', [[ 0, "desc" ]])
					.withOption('stateSave', true)
					.withPaginationType('full_numbers');
			vm.dtColumnDefs = [
				DTColumnDefBuilder.newColumnDef(0),
				DTColumnDefBuilder.newColumnDef(1),
				DTColumnDefBuilder.newColumnDef(2).notSortable(),
			];
		}
		vm.activeTab = tab;
	}

	function addSubutaiTemplateForm() {
		ngDialog.open({
			template: 'subutai-app/kurjun/partials/domainForm.html',
			scoep: $scope
		});
	}

	function proceedTemplate(test) {
		console.log(test);
	}

	function getTemplates(index, repositories) {
		kurjunSrv.getTemplates(repositories[index]).then(function (templates) {
			for(var template in templates.data) {
				templates.data[template].repository = repositories[index];
				vm.templates.push(templates.data[template]);
			}
			if(( index + 1 )<repositories.length) {
				getTemplates(index + 1, repositories);
			} else {
				return;
			}
		});
	}

	function deleteTemplate(template) {
		kurjunSrv.deleteTemplate(template.md5Sum, template.repository).success(function () {
			kurjunSrv.getRepositories().success(function (repositories) {
				vm.repositories = repositories;
				vm.templates = [];
				getTemplates(0, repositories);
			});
		});
	}

	function addTemplate(repository) {
		kurjunSrv.addTemplate(repository, vm.file);
	}

	function addAptTemplate() {
		kurjunSrv.addAptTemplate(vm.file);
	}


	function addAPTForm() {
		ngDialog.open({
			template: 'subutai-app/kurjun/partials/apt-form.html',
			scoep: $scope
		});
	}

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
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

