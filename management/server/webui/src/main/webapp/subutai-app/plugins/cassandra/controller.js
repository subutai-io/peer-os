'use strict';

angular.module('subutai.plugins.cassandra.controller', [])
    .controller('CassandraCtrl', CassandraCtrl)
	.directive('colSelectContainers', colSelectContainers)
	.directive('colSelectSeeds', colSelectSeeds);

CassandraCtrl.$inject = ['cassandraSrv', 'SweetAlert'];
function CassandraCtrl(cassandraSrv, SweetAlert) {
    var vm = this;
	vm.activeTab = 'install';
	vm.cassandraInstall = {};
	vm.containers = [];
	vm.seeds = [];

    //cassandraSrv.getCassandra()(function (data) {
    //    vm.cassandra= data;
    //});
	
	//functions
	vm.showContainers = showContainers;
	vm.addContainer = addContainer;
	vm.addSeed = addSeed;
	vm.createCassandra = createCassandra;
	
	setDefaultValues();
	cassandraSrv.getEnvironments().success(function (data) {
		vm.environments = data;
	});

	function createCassandra() {
		cassandraSrv.createCassandra(JSON.stringify(vm.cassandraInstall)).success(function (data) {
			SweetAlert.swal("Success!", "Your Cassandra cluster start creating.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cassandra cluster create error: ' + error.ERROR, "error");
		});
	}

	function showContainers(environmentId) {
		vm.containers = [];
		vm.seeds = [];		
		for(var i in vm.environments) {
			if(environmentId == vm.environments[i].id) {
				for (var j = 0; j < vm.environments[i].containers.length; j++){
					if(vm.environments[i].containers[j].templateName == 'cassandra') {
						vm.containers.push(vm.environments[i].containers[j]);
					}
				}
				break;
			}
		}
	}

	function addContainer(containerId) {
		if(vm.cassandraInstall.containerId.indexOf(containerId) > -1) {
			vm.cassandraInstall.containerId.splice(vm.cassandraInstall.containerId.indexOf(containerId), 1);
		} else {
			vm.cassandraInstall.containerId.push(containerId);
		}
		vm.seeds = angular.copy(vm.cassandraInstall.containerId);
	}

	function addSeed(seedId) {
		if(vm.cassandraInstall.seedId.indexOf(seedId) > -1) {
			vm.cassandraInstall.seedId.splice(vm.cassandraInstall.seedId.indexOf(seedId), 1);
		} else {
			vm.cassandraInstall.seedId.push(seedId);
		}
	}
	
	function setDefaultValues() {
		vm.cassandraInstall.dataDir = '/var/lib/cassandra/data';
		vm.cassandraInstall.commitDir = '/var/lib/cassandra/commitlog';
		vm.cassandraInstall.cacheDir = '/var/lib/cassandra/saved_caches';
		vm.cassandraInstall.containerId = [];
		vm.cassandraInstall.seedId = [];
	}
}

function colSelectContainers() {
	return {
		restrict: 'E',
		templateUrl: 'subutai-app/plugins/cassandra/directives/col-select/col-select-containers.html'
	}
};

function colSelectSeeds() {
	return {
		restrict: 'E',
		templateUrl: 'subutai-app/plugins/cassandra/directives/col-select/col-select-seeds.html'
	}
};

