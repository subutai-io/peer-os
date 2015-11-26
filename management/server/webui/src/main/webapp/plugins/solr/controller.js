'use strict';

angular.module('subutai.plugins.solr.controller', [])
    .controller('SolrCtrl', SolrCtrl)
	.directive('colSelectContainers', colSelectContainers)
	.directive('colSelectSeeds', colSelectSeeds);

SolrCtrl.$inject = ['solrSrv', 'SweetAlert'];
function SolrCtrl(solrSrv, SweetAlert) {
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
	solrSrv.getEnvironments().success(function (data) {
		vm.environments = data;
	});

	function getClusters() {
		solrSrv.getClusters().success(function (data) {
			vm.clusters = data;
		});
	}
	getClusters();

	function getClustersInfo(selectedCluster) {
		solrSrv.getClusters(selectedCluster).success(function (data) {
			vm.currentCluster = data;
			console.log(vm.currentCluster);
		});
	}

	function startNodes() {
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.name === undefined) return;
		solrSrv.startNodes(vm.currentCluster.name, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster nodes started successfully.", "success");
			getClustersInfo(vm.currentCluster.name);
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster start error: ' + error.ERROR, "error");
		});
	}

	function stopNodes() {
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.name === undefined) return;
		solrSrv.stopNodes(vm.currentCluster.name, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster nodes stoped successfully.", "success");
			getClustersInfo(vm.currentCluster.name);
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
		if(vm.currentCluster.name === undefined) return;
		SweetAlert.swal("Success!", "Adding node action started.", "success");
		solrSrv.addNode(vm.currentCluster.name).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Node has been added on cluster " + vm.currentCluster.name + ".",
				"success"
			);
			getClustersInfo(vm.currentCluster.name);
		});
	}

	function deleteNode(nodeId) {
		if(vm.currentCluster.name === undefined) return;
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
				solrSrv.deleteNode(vm.currentCluster.name, nodeId).success(function (data) {
					SweetAlert.swal("Deleted!", "Node has been deleted.", "success");
					vm.currentCluster = {};
				});
			}
		});
	}

	function deleteCluster() {
		if(vm.currentCluster.name === undefined) return;
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
				solrSrv.deleteCluster(vm.currentCluster.name).success(function (data) {
					SweetAlert.swal("Deleted!", "Cluster has been deleted.", "success");
					vm.currentCluster = {};
				});
			}
		});
	}

	function createCassandra() {
		solrSrv.createCassandra(JSON.stringify(vm.cassandraInstall)).success(function (data) {
			SweetAlert.swal("Success!", "Your Cassandra cluster start creating.", "success");
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cassandra cluster create error: ' + error.ERROR, "error");
		});
	}

	function changeClusterScaling(scale) {
		if(vm.currentCluster.name === undefined) return;
		try {
			solrSrv.changeClusterScaling(vm.currentCluster.name, scale);
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
		templateUrl: 'plugins/cassandra/directives/col-select/col-select-containers.html'
	}
};

function colSelectSeeds() {
	return {
		restrict: 'E',
		templateUrl: 'plugins/cassandra/directives/col-select/col-select-seeds.html'
	}
};

