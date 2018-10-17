'use strict';

angular.module('subutai.containers.controller', ['ngTagsInput'])
	.controller('ContainerViewCtrl', ContainerViewCtrl)
	.filter('getEnvById', function() {
		return function(input, id) {
			for ( var i = 0; i < input.length ; i++ )
			{
				if (input[i].id == id) {
					return input[i].name;
				}
			}
			return null;
		}
	});

ContainerViewCtrl.$inject = ['$scope', '$rootScope', 'environmentService', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', '$stateParams', 'ngDialog', '$timeout', 'cfpLoadingBar', 'identitySrv', 'templateSrv'];

function ContainerViewCtrl($scope, $rootScope, environmentService, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, $stateParams, ngDialog, $timeout, cfpLoadingBar, identitySrv, templateSrv) {

//	checkCDNToken(templateSrv, $rootScope)

	var vm = this;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.environments = [];
	vm.containers = [];
	vm.notRegisteredContainers = [];
	vm.containersType = [];
	vm.environmentId = $stateParams.environmentId;
	vm.currentTags = [];
	vm.allTags = [];
	vm.tags2Container = {};
	vm.currentDomainStatus = {};
	vm.currentDomainPort = {};
	vm.domainContainer = {};
	vm.editingContainer = {};
	vm.hasPGPplugin = false;
	vm.bazaarStatus = false;
	$timeout(function() {
		vm.bazaarStatus = bazaarRegisterStatus;
		vm.hasPGPplugin = hasPGPplugin();
	}, 2000);

	// functions
	vm.filterContainersList = filterContainersList;
	vm.containerAction = containerAction;
	vm.destroyContainer = destroyContainer;
	vm.destroyNotRegisteredContainer = destroyNotRegisteredContainer;
	vm.addTagForm = addTagForm;
	vm.addTags = addTags;
	vm.removeTag = removeTag;
	vm.showDomainForm = showDomainForm;
	vm.setContainerDomain = setContainerDomain;
	vm.getContainerStatus = getContainerStatus;
	vm.setContainerName = setContainerName;
	vm.changeNamePopup = changeNamePopup;
	vm.createTemplatePopup=createTemplatePopup;
	vm.createTemplate=createTemplate;
	vm.hasCdnToken=hasCdnToken;
    vm.isAdmin = isAdmin;

	environmentService.getContainersType().success(function (data) {
		vm.containersType = data;
	});

    function isAdmin(){
        return localStorage.getItem('isAdmin') == 'true';
    }

	function alertForBazaarContainer( container )
	{
        if (container.dataSource != "subutai") {

            SweetAlert.swal("Feature coming soon...", "This container is created on Bazaar. Please use Bazaar to manage it.", "success");

            return true;
        }

		return false;
	}

	function showDomainForm(container) {

        if (alertForBazaarContainer(container)) {
            return;
        }

		LOADING_SCREEN();
		vm.currentDomainStatus = {};
		vm.currentDomainPort = {};
		vm.domainContainer = container;
		environmentService.getContainerDomainNPort(container).success(function (data) {
			vm.currentDomainStatus = data.status == "true";
			vm.currentDomainPort = parseInt( data.port, 10);
			ngDialog.open({
				template: 'subutai-app/containers/partials/addToDomain.html',
				scope: $scope
			});			
			LOADING_SCREEN('none');
		}).error(function(error){
			LOADING_SCREEN('none');
			SweetAlert.swal ("ERROR!", error.replace(/\\n/g, " "));
			ngDialog.closeAll();
		});
	}

	function setContainerDomain() {
		environmentService.setContainerDomainNPort(vm.domainContainer, vm.currentDomainStatus, vm.currentDomainPort).success(function (data) {
			vm.currentDomainStatus = data.status == "true";
			vm.currentDomainPort = parseInt( data.port, 10);
		}).error(function(error){
          			LOADING_SCREEN('none');
          			SweetAlert.swal ("ERROR!", error.replace(/\\n/g, " "));
          			ngDialog.closeAll();
        });
		ngDialog.closeAll();
	}

	function addTagForm(container) {

        if (alertForBazaarContainer(container)) {
            return;
        }

		vm.tags2Container = container;
		vm.currentTags = [];
		for(var i = 0; i < container.tags.length; i++) {
			vm.currentTags.push({text: container.tags[i]});
		}
		ngDialog.open({
			template: 'subutai-app/containers/partials/addTagForm.html',
			scope: $scope
		});
	}

	function addTags() {
		var tags = [];
		for(var i = 0; i < vm.currentTags.length; i++){
			tags.push(vm.currentTags[i].text);
		}
		environmentService.setTags(vm.tags2Container.environmentId, vm.tags2Container.id, tags).success(function (data) {
			vm.tags2Container.tags = tags;
		});
		vm.tags2Container.tags = tags;
		ngDialog.closeAll();
	}

	function removeTag(container, tag, key) {
		environmentService.removeTag(container.environmentId, container.id, tag).success(function (data) {
		});
		container.tags.splice(key, 1);
	}

	function getNotRegisteredContainers() {
		environmentService.getNotRegisteredContainers().success(function (data) {
			vm.notRegisteredContainers = data;
		});
	}
	getNotRegisteredContainers();

	function destroyNotRegisteredContainer(containerId, key) {
		var previousWindowKeyDown = window.onkeydown;
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this Container!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Destroy",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			window.onkeydown = previousWindowKeyDown;
			if (isConfirm) {
				environmentService.deleteNotRegisteredContainer(containerId).success(function (data) {
					SweetAlert.swal("Destroyed!", "Your container has been destroyed.", "success");
					vm.notRegisteredContainers.splice(key, 1);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your Container is safe. Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function getContainers() {
		environmentService.getEnvironments().success(function (data) {

			for(var i = 0; i < data.length; i++) {
				data[i].containers.sort(compare);
			}
			data.sort(compare);

			var currentArrayString = JSON.stringify(vm.environments, function( key, value ) {
				if( key === "$$hashKey" ) {
					return undefined;
				}
				return value;
			});
			var serverArrayString = JSON.stringify(data, function( key, value ) {
				if( key === "$$hashKey" ) {
					return undefined;
				}
				return value;
			});

			if(currentArrayString != serverArrayString) {
				vm.environments = data;
			}
			filterContainersList();
		});
	}
	getContainers();

	function compare(a,b) {
		if (a.id < b.id) return -1;
		if (a.id > b.id) return 1;
		return 0;
	}

	function filterContainersList() {
		vm.allTags = [];
		vm.containers = [];

		if(vm.environmentId != 'ORPHAN') {
			for(var i in vm.environments) {

				if(
					vm.environmentId == vm.environments[i].id || 
					vm.environmentId === undefined || 
					vm.environmentId.length == 0
				) {
					for(var j in vm.environments[i].containers) {
						if(
							vm.containersTypeId !== undefined && 
							vm.containersTypeId != vm.environments[i].containers[j].type && 
							vm.containersTypeId.length > 0
						) {continue;}
						if(
							vm.containerState !== undefined && 
							vm.containerState != vm.environments[i].containers[j].state && 
							vm.containerState.length > 0
						) {continue;}

						// We don't show on UI containers created bybazaar, located on other peers.
						// See details: io.subutai.core.environment.impl.adapter.EnvironmentAdapter.
						// @todo remove when implement on backend
						var container = vm.environments[i].containers[j];
						var remoteProxyContainer = !container.local && container.dataSource != "subutai";

						if ( !remoteProxyContainer )
						{
							vm.containers.push(vm.environments[i].containers[j]);
							vm.allTags = vm.allTags.concat(vm.environments[i].containers[j].tags);
						}
					}
				}
			}
		}
	}

	vm.dtOptions = DTOptionsBuilder
		.newOptions()
		.withOption('order', [[ 2, "asc" ]])
		.withOption('stateSave', true)
		.withPaginationType('full_numbers');
	vm.dtColumnDefs = [
		DTColumnDefBuilder.newColumnDef(0),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2),
		DTColumnDefBuilder.newColumnDef(3).notSortable(),
		DTColumnDefBuilder.newColumnDef(4).notSortable(),
		DTColumnDefBuilder.newColumnDef(5).notSortable(),
		DTColumnDefBuilder.newColumnDef(6).notSortable()
	];

	function destroyContainer(containerId, key) {
		var previousWindowKeyDown = window.onkeydown;
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this Container!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Destroy",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			window.onkeydown = previousWindowKeyDown;
			if (isConfirm) {
				environmentService.destroyContainer(containerId).success(function (data) {
					SweetAlert.swal("Destroyed!", "Your container has been destroyed.", "success");
					vm.containers.splice(key, 1);
				}).error(function (data) {
					SweetAlert.swal("ERROR!", "Your environment is safe :). Error: " + data.ERROR, "error");
				});
			}
		});
	}

	function containerAction(key) {
		var action = 'start';
		if(vm.containers[key].state == 'RUNNING') {
			action = 'stop';
			vm.containers[key].state = 'STOPPING';
		} else {
			vm.containers[key].state = 'STARTING';
		}

		environmentService.switchContainer(vm.containers[key].id, action).success(function (data) {
			if(vm.containers[key].state == 'STOPPING') {
				vm.containers[key].state = 'STOPPED';
			} else {
				vm.containers[key].state = 'RUNNING';
			}
		}).error(function (data) {
            vm.containers[key].state = 'STOPPED';
            SweetAlert.swal("ERROR!", data, "error");
        });
	}

	function getContainerStatus(container) {
		container.state = 'checking';
		environmentService.getContainerStatus(container.id).success(function (data) {
			container.state = data.STATE;
		});
	}

	function setContainerName( container, name ) {

        if (! /^[a-zA-Z][a-zA-Z0-9\-]{0,49}$/.test(name)){
                    SweetAlert.swal("Invalid hostname", "The container hostname must start with a letter and have as interior characters only letters, digits, and hyphen", "error");

                    return;
        }

		LOADING_SCREEN();
		environmentService.setContainerName( container, name ).success( function (data) {
			location.reload();
		} ).error( function (error) {
		    ngDialog.closeAll();
		    LOADING_SCREEN('none');
			SweetAlert.swal ("ERROR!", $.trim(error) ? error: 'Invalid hostname', "error");
		} );
	}


	function changeNamePopup( container ) {

        if (alertForBazaarContainer(container)) {
            return;
        }

		vm.editingContainer = container;

		ngDialog.open({
			template: 'subutai-app/containers/partials/changeName.html',
			scope: $scope,
			className: 'b-build-environment-info'
		});
	}

	function createTemplatePopup(container){

//        if( hasCdnToken() ){
            vm.editingContainer = container;

            ngDialog.open({
                template: 'subutai-app/containers/partials/createTemplate.html',
                scope: $scope,
                className: 'b-build-environment-info'
            });
//		} else {
//		    SweetAlert.swal(
//		    "Your key is not registered with Bazaar",
//		    "Please, register your key on Bazaar",
//		    "success");
//		}
	}


    var timeout;
    vm.uploadPercent;
    function showUploadProgress(templateName, isScheduled){

        ngDialog.open({
            template: 'subutai-app/containers/partials/uploadProgress.html',
            scope: $scope,
            className: 'b-build-environment-info'
        });

        clearTimeout(timeout);

        environmentService.getUploadProgress(templateName).success(function (data) {

            if($.trim(data) && $.trim(data.templatesUploadProgress) && data.templatesUploadProgress.length > 0
                 && $.trim(data.templatesUploadProgress[0].templatesUploadProgress)
                  && $.trim(data.templatesUploadProgress[0].templatesUploadProgress[templateName])){

                var percent = parseInt(data.templatesUploadProgress[0].templatesUploadProgress[templateName]);

                if (percent != 100) {
                    timeout = setTimeout (function(){ showUploadProgress(templateName, true); }, 3000);
                }

                if(isScheduled) vm.uploadPercent = isNaN(percent) ? 0: percent;

            }else{
                timeout = setTimeout (function(){ showUploadProgress(templateName, true); }, 3000)
            }
        })
        .error(function (error) {

            console.log(error);
        });
    }


    vm.disabled = false;
    function createTemplate( container, name, version ) {

        if (! /^[a-zA-Z][a-zA-Z0-9\-]{0,49}$/.test(name)){
                    SweetAlert.swal("Invalid name", "The template name must start with a letter and have as interior characters only letters, digits, and hyphen", "error");

                    return;
        }

        if (! /^\d\.\d\.\d$/.test(version)){
                    SweetAlert.swal("Invalid version", "The template version must be in form d.d.d where d is a digit. E.g. 1.2.3", "error");

                    return;
        }

        clearTimeout(timeout);

        vm.disabled = true;

        vm.uploadPercent = 0;

        ngDialog.closeAll();

        checkCDNToken(templateSrv, $rootScope, function(){

                showUploadProgress(name);

                environmentService.createTemplate( container, name, version, false )
                .success(function(){
                    vm.disabled = false;
                    ngDialog.closeAll();
                    clearTimeout(timeout);
                    SweetAlert.swal ("Success!", "Template has been created", "success");
                 })
                .error( function (error) {
                    vm.disabled = false;
                    ngDialog.closeAll();
                    clearTimeout(timeout);
                    SweetAlert.swal ("ERROR!", error.ERROR, "error");
                } );

        });

    }

    function hasCdnToken(){
        return !(localStorage.getItem('cdnToken') == undefined || localStorage.getItem('cdnToken') == null);
    }

}
