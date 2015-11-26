'use strict';

angular.module('subutai.plugins.hadoop.controller', [])
    .controller('HadoopCtrl', HadoopCtrl)
	.directive('colSelectContainers', colSelectContainers)
	.directive('checkboxListDropdown', checkboxListDropdown);

HadoopCtrl.$inject = ['hadoopSrv', 'SweetAlert'];

function HadoopCtrl(hadoopSrv, SweetAlert)
{
    var vm = this;
	vm.activeTab = 'install';
	vm.hadoopInstall = {};
	vm.environments = [];
	vm.containers = [];
	vm.clusters = [];

	//functions
	vm.createHadoop = createHadoop;
	vm.showContainers = showContainers;
	vm.addContainer = addContainer;
	vm.getClustersInfo = getClustersInfo;

	setDefaultValues();

	hadoopSrv.getEnvironments().success(function (data) {
		vm.environments = data;
	});

	function getClusters() {
		hadoopSrv.getClusters().success(function (data) {
			vm.clusters = data;
			console.log(vm.clusters);
		});
	}
	getClusters();

	function getClustersInfo(selectedCluster) {
		LOAD_SCREEN();
		hadoopSrv.getClusters(selectedCluster).success(function (data) {
			vm.currentCluster = data;
			console.log(vm.currentCluster);
			LOAD_SCREEN('none');
		});
	}

	function createHadoop() {
		SweetAlert.swal("Success!", "Hadoop cluster start creating.", "success");
		hadoopSrv.createHadoop(JSON.stringify(vm.hadoopInstall)).success(function (data) {
			SweetAlert.swal("Success!", "Hadoop cluster create successfully.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Hadoop cluster create error: ' + error, "error");
		});
	}

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
		if(vm.hadoopInstall.slaves.indexOf(containerId) > -1) {
			vm.hadoopInstall.slaves.splice(vm.hadoopInstall.slaves.indexOf(containerId), 1);
		} else {
			vm.hadoopInstall.slaves.push(containerId);
		}
	}	

	function setDefaultValues() {
		vm.hadoopInstall.domainName = 'intra.lan';
		vm.hadoopInstall.slaves = [];
	}

}

function colSelectContainers() {
	return {
		restrict: 'E',
		templateUrl: 'plugins/hadoop/directives/col-select/col-select-containers.html'
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

