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
	vm.environments = [];
	vm.containers = [];
	vm.seeds = [];

	vm.clusters = [];
	vm.currentCluster = {};
	vm.nodes2Action = [];

	//functions
	vm.showContainers = showContainers;
	vm.addContainer = addContainer;
	vm.addSeed = addSeed;
	vm.createCassandra = createCassandra;

	vm.getClustersInfo = getClustersInfo;
	vm.changeClusterScaling = changeClusterScaling;
	vm.deleteCluster = deleteCluster;
	vm.addNode = addNode;
	vm.deleteNode = deleteNode;
	vm.pushNode = pushNode;
	vm.startNodes = startNodes;
	vm.stopNodes = stopNodes;
	
	setDefaultValues();
	cassandraSrv.getEnvironments().success(function (data) {
		vm.environments = data;
	});

	function getClusters() {
		cassandraSrv.getClusters().success(function (data) {
			vm.clusters = data;
		});
	}
	getClusters();

	function getClustersInfo(selectedCluster) {
		cassandraSrv.getClusters(selectedCluster).success(function (data) {
			vm.currentCluster = data;
			console.log(vm.currentCluster);
		});
	}

	function startNodes() {
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.clusterName === undefined) return;
		cassandraSrv.startNodes(vm.currentCluster.clusterName, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster nodes started successfully.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster start error: ' + error.ERROR, "error");
		});
	}

	function stopNodes() {
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.clusterName === undefined) return;
		cassandraSrv.stopNodes(vm.currentCluster.clusterName, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster nodes stoped successfully.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster stop error: ' + error.ERROR, "error");
		});
	}

	function pushNode(id) {
		if(vm.nodes2Action.indexOf(id) >= 0) {
			vm.nodes2Action.splice(vm.nodes2Action.indexOf(id), 1);
		} else {
			vm.nodes2Action.push(id);
		}
	}

	function addNode() {
		if(vm.currentCluster.clusterName === undefined) return;
		SweetAlert.swal("Success!", "Adding node action started.", "success");
		cassandraSrv.addNode(vm.currentCluster.clusterName).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Node has been added on cluster " + vm.currentCluster.clusterName + ".",
				"success"
			);
			getClustersInfo(vm.currentCluster.clusterName);
		});
	}

	function deleteNode(nodeId) {
		if(vm.currentCluster.clusterName === undefined) return;
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this node!",
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
				cassandraSrv.deleteNode(vm.currentCluster.clusterName, nodeId).success(function (data) {
					SweetAlert.swal("Deleted!", "Node has been deleted.", "success");
					vm.currentCluster = {};
				});
			}
		});
	}

	function deleteCluster() {
		if(vm.currentCluster.clusterName === undefined) return;
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your will not be able to recover this cluster!",
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
				cassandraSrv.deleteCluster(vm.currentCluster.clusterName).success(function (data) {
					SweetAlert.swal("Deleted!", "Cluster has been deleted.", "success");
					vm.currentCluster = {};
				});
			}
		});
	}

	function createCassandra() {
		cassandraSrv.createCassandra(JSON.stringify(vm.cassandraInstall)).success(function (data) {
			SweetAlert.swal("Success!", "Your Cassandra cluster start creating.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cassandra cluster create error: ' + error.ERROR, "error");
		});
	}

	function changeClusterScaling(scale) {
		if(vm.currentCluster.clusterName === undefined) return;
		try {
			cassandraSrv.changeClusterScaling(vm.currentCluster.clusterName, scale);
		} catch(e) {}
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
		if(vm.cassandraInstall.containers.indexOf(containerId) > -1) {
			vm.cassandraInstall.containers.splice(vm.cassandraInstall.containers.indexOf(containerId), 1);
		} else {
			vm.cassandraInstall.containers.push(containerId);
		}
		vm.seeds = angular.copy(vm.cassandraInstall.containers);
	}

	function addSeed(seedId) {
		if(vm.cassandraInstall.seeds.indexOf(seedId) > -1) {
			vm.cassandraInstall.seeds.splice(vm.cassandraInstall.seeds.indexOf(seedId), 1);
		} else {
			vm.cassandraInstall.seeds.push(seedId);
		}
	}
	
	function setDefaultValues() {
		vm.cassandraInstall.domainName = 'intra.lan';
		vm.cassandraInstall.dataDir = '/var/lib/cassandra/data';
		vm.cassandraInstall.commitDir = '/var/lib/cassandra/commitlog';
		vm.cassandraInstall.cacheDir = '/var/lib/cassandra/saved_caches';
		vm.cassandraInstall.containers = [];
		vm.cassandraInstall.seeds = [];
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

