'use strict';

angular.module('subutai.plugins.hadoop.controller', [])
    .controller('HadoopCtrl', HadoopCtrl)
	.directive('colSelectContainers', colSelectContainers)
	.directive('checkboxListDropdown', checkboxListDropdown);

HadoopCtrl.$inject = ['hadoopSrv'];

function HadoopCtrl(hadoopSrv)
{
    var vm = this;
	vm.activeTab = 'install';
	vm.hadoopInstall = {};
	vm.environments = [];
	vm.containers = [];

	//functions
	vm.showContainers = showContainers;
	vm.addContainer = addContainer;

	setDefaultValues();
	hadoopSrv.getEnvironments().success(function (data) {
		vm.environments = data;
	});

	function showContainers(environmentId) {
		vm.containers = [];
		vm.seeds = [];		
		for(var i in vm.environments) {
			if(environmentId == vm.environments[i].id) {
				for (var j = 0; j < vm.environments[i].containers.length; j++){
					if(vm.environments[i].containers[j].templateName == 'hadoop') {
						vm.containers.push(vm.environments[i].containers[j]);
					}
				}
				break;
			}
		}
	}

	function addContainer(containerId) {
		if(vm.hadoopInstall.containerId.indexOf(containerId) > -1) {
			vm.hadoopInstall.containerId.splice(vm.hadoopInstall.containerId.indexOf(containerId), 1);
		} else {
			vm.hadoopInstall.containerId.push(containerId);
		}
		vm.seeds = angular.copy(vm.hadoopInstall.containerId);
	}	

	function setDefaultValues() {
		vm.hadoopInstall.containerId = [];
	}

}

function colSelectContainers() {
	return {
		restrict: 'E',
		templateUrl: 'subutai-app/plugins/hadoop/directives/col-select/col-select-containers.html'
	}
};

function checkboxListDropdown() {
	return {
		restrict: 'A',
		link: function(scope, element, attr) {
			$(".b-form-input_dropdown").click(function () {
				$(this).toggleClass("is-active");
			});

			$(".b-form-input-dropdown-list").click(function(e) {
				e.stopPropagation();
			});
		}
	}
};

