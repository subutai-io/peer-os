'use strict';

angular.module('subutai.kurjun.controller', [])
	.controller('KurjunCtrl', KurjunCtrl)
	.directive('fileModel', fileModel);

KurjunCtrl.$inject = ['$scope', '$rootScope', 'kurjunSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', '$resource', '$compile', 'ngDialog', '$timeout', 'cfpLoadingBar'];
fileModel.$inject = ['$parse'];

var fileUploder = {};

function KurjunCtrl($scope, $rootScope, kurjunSrv, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, $resource, $compile, ngDialog, $timeout, cfpLoadingBar) {

	var vm = this;
	vm.activeTab = 'templates';
	vm.repositories = [];
	vm.templates = [];
	vm.apts = [];
	vm.isUploadAllowed = false;
	vm.listOfUsers = [];
	vm.users2Add = [];

	vm.openTab = openTab;
	vm.addTemplate = addTemplate;
	vm.proceedTemplate = proceedTemplate;
	vm.deleteTemplate = deleteTemplate;
	vm.deleteAPT = deleteAPT;
	vm.openShareWindow = openShareWindow;
	vm.shareTemplate = shareTemplate;
	vm.checkRepositoryStatus = checkRepositoryStatus;
	vm.setDefaultRepository = setDefaultRepository;

	vm.addUser2Stack = addUser2Stack;
	vm.removeUserFromStack = removeUserFromStack;

	kurjunSrv.getCurrentUser().success (function (data) {
		vm.currentUser = data;
	});


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
			.withOption('order', [[0, "desc"]])
			.withOption('stateSave', true)
			.withPaginationType('full_numbers');

		switch (tab) {
			case 'templates':
				vm.dtColumnDefs = [
					DTColumnDefBuilder.newColumnDef(0),
					DTColumnDefBuilder.newColumnDef(1),
					DTColumnDefBuilder.newColumnDef(2),
					DTColumnDefBuilder.newColumnDef(3),
					DTColumnDefBuilder.newColumnDef(4),
					DTColumnDefBuilder.newColumnDef(5).notSortable()
				];
				break;
			case 'apt':
				vm.dtColumnDefs = [
					DTColumnDefBuilder.newColumnDef(0),
					DTColumnDefBuilder.newColumnDef(1),
					DTColumnDefBuilder.newColumnDef(2),
					DTColumnDefBuilder.newColumnDef(3),
					DTColumnDefBuilder.newColumnDef(4).notSortable()
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
				vm.currentTemplate = {repository: null, file: null};
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
							SweetAlert.swal("ERROR!", response.data, "error");
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
							SweetAlert.swal("ERROR!", response.data, "error");
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
						SweetAlert.swal("Deleted!", "Template has been deleted.", "success");
						getTemplates();
					}).error(function (data) {
						LOADING_SCREEN('none');
						SweetAlert.swal("ERROR!", data, "error");
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
						SweetAlert.swal("Deleted!", "APT package has been deleted.", "success");
						getAPTs();
					}).error(function (data) {
						LOADING_SCREEN('none');
						SweetAlert.swal("ERROR!", data, "error");
					});
				}
			});
	}

	function openShareWindow(template) {
		vm.listOfUsers = [];
		vm.checkedUsers = [];
		kurjunSrv.getUsers().success(function (data) {
			for (var i = 0; i < data.length; ++i) {
				if (data[i].id !== vm.currentUser.id) {
					vm.listOfUsers.push (data[i]);
				}
			}
			for (var i = 0; i < vm.listOfUsers.length; ++i) {
				vm.listOfUsers[i].read = true;
				vm.listOfUsers[i].write = true;
				vm.listOfUsers[i].update = true;
				vm.listOfUsers[i].delete = true;
			}
			kurjunSrv.getShared(template.id).success(function (data2) {
				vm.users2Add = data2;
				for (var i = 0; i < vm.users2Add.length; ++i) {
					if (vm.users2Add[i].id === vm.currentUser.id) {
						vm.users2Add.splice (i, 1);
						--i;
						continue;
					}
					for (var j = 0; j < vm.listOfUsers.length; ++j) {
						if (vm.listOfUsers[j].id === vm.users2Add[i].id) {
							vm.users2Add[i].fullName = vm.listOfUsers[j].fullName;
							vm.listOfUsers.splice (j, 1);
							break;
						}
					}
				}
				vm.currentTemplate = angular.copy(template);
				ngDialog.open ({
					template: "subutai-app/environment/partials/popups/share-template.html",
					scope: $scope
				});
			});
		});
	}

	function addUser2Stack(user) {
		vm.users2Add.push(angular.copy(user));
		for (var i = 0; i < vm.listOfUsers.length; ++i) {
			if (vm.listOfUsers[i].fullName === user.fullName) {
				vm.listOfUsers.splice (i, 1);
				break;
			}
		}
	}


	function removeUserFromStack(key) {
		vm.listOfUsers.push (vm.users2Add[key]);
		vm.users2Add.splice(key, 1);
	}

	function shareTemplate() {
		var users = [];
		for (var i = 0; i < vm.users2Add.length; ++i) {
			users.push ({
				id: vm.users2Add[i].id,
				read: vm.users2Add[i].read,
				write: vm.users2Add[i].write,
				update: vm.users2Add[i].update,
				delete: vm.users2Add[i].delete
			});
		}
		kurjunSrv.shareTemplate(JSON.stringify (users), vm.currentTemplate.id).success(function (data) {
			SweetAlert.swal("Success!", "Your template was successfully shared.", "success");
			ngDialog.closeAll();
		}).error(function (data) {
			SweetAlert.swal("ERROR!", data.ERROR, "error");
		});
	}


	function checkRepositoryStatus(repository) {
		kurjunSrv.isUploadAllowed(repository).success(function (data) {
			vm.isUploadAllowed = (data === 'false' ? false : true);
		});
	}

	function setDefaultRepository() {
		vm.currentTemplate.repository = vm.repositories[0];
		vm.checkRepositoryStatus(vm.currentTemplate.repository)
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
		link: function (scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function () {
				document.getElementById("js-uploadFile").value = element[0].files[0].name;
				scope.$apply(function () {
					modelSetter(scope, element[0].files[0]);
					fileUploder = element[0].files[0];
				});
			});
		}
	};
}

