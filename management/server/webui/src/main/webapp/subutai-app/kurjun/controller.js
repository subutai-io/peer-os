'use strict';

angular.module('subutai.kurjun.controller', [])
	.controller('KurjunViewCtrl', KurjunViewCtrl)
	.directive('fileModel', fileModel);

KurjunViewCtrl.$inject = ['$scope', '$rootScope', 'kurjunService', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', '$resource', '$compile', 'ngDialog', '$timeout', 'cfpLoadingBar'];
fileModel.$inject = ['$parse'];

var fileUploder = {};

function KurjunViewCtrl($scope, $rootScope, kurjunService, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, $resource, $compile, ngDialog, $timeout, cfpLoadingBar) {

	var vm = this;
	vm.activeTab = 'repositories';
	vm.repositories = [];
	vm.templates = [];
	vm.apts = [];

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

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

