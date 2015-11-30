'use strict';

angular.module('subutai.plugins.presto.controller', [])
    .controller('PrestoCtrl', PrestoCtrl)
	.directive('colSelectPrestoNodes', colSelectPrestoNodes);

PrestoCtrl.$inject = ['$scope', 'prestoSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', 'ngDialog'];

function PrestoCtrl($scope, prestoSrv, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, ngDialog) {
    var vm = this;
	vm.activeTab = 'install';
	vm.prestoInstall = {};
	vm.clusters = [];
	vm.hadoopClusters = [];
	vm.currentClusterNodes = [];
	vm.currentCluster = {};
	vm.availableNodes = [];
	vm.otherNodes = [];
	vm.nodes2Action = [];


	//functions
	vm.getClustersInfo = getClustersInfo;	
	vm.getHadoopClusterNodes = getHadoopClusterNodes;	
	vm.addContainer = addContainer;	
	vm.createPresto = createPresto;
	vm.deleteNode = deleteNode;
	vm.addNodeForm = addNodeForm;
	vm.addNode = addNode;
	vm.deleteCluster = deleteCluster;
	vm.changeDirective = changeDirective;
	vm.startServer = startServer;
	vm.stopServer = stopServer;
	vm.pushNode = pushNode;
	vm.pushAll = pushAll;
	vm.changeClusterScaling = changeClusterScaling;
	vm.startNodes = startNodes;
	vm.stopNodes = stopNodes;

	prestoSrv.getHadoopClusters().success(function(data){
		vm.hadoopClusters = data;
		if(vm.hadoopClusters.length == 0) {
			SweetAlert.swal("ERROR!", 'No Hadoop clusters was found! Create Hadoop cluster first.', "error");
		}
	}).error(function(data){
		SweetAlert.swal("ERROR!", 'No Hadoop clusters was found! ERROR: ' + data, "error");
	});
	setDefaultValues();

	function getClusters() {
		prestoSrv.getClusters().success(function (data) {
			vm.clusters = data;
		});
	}
	getClusters();

	vm.dtOptions = DTOptionsBuilder
		.newOptions()
		.withOption('order', [[0, "asc" ]])
		.withOption('stateSave', true)
		.withPaginationType('full_numbers');

	vm.dtColumnDefs = [
		DTColumnDefBuilder.newColumnDef(0),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2).notSortable()
	];

	function getClustersInfo(selectedCluster) {
		LOADING_SCREEN();
		vm.currentCluster = {};
		prestoSrv.getClusters(selectedCluster).success(function (data) {
			vm.currentCluster = data;
			console.log (vm.currentCluster);
			LOADING_SCREEN('none');
		});
	}

	function addNodeForm() {
		if(vm.currentCluster.clusterName === undefined) return;
		prestoSrv.getAvailableNodes(vm.currentCluster.clusterName).success(function (data) {
			vm.availableNodes = data;
		});
		ngDialog.open({
			template: 'plugins/presto/partials/addNodesForm.html',
			scope: $scope
		});
	}

	function addNode(chosenNode) {
		if(chosenNode === undefined) return;
		if(vm.currentCluster.clusterName === undefined) return;
		SweetAlert.swal("Success!", "Adding node action started.", "success");
		ngDialog.closeAll();
		prestoSrv.addNode(vm.currentCluster.clusterName, chosenNode).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Node has been added on cluster " + vm.currentCluster.clusterName + ".",
				"success"
			);
			getClustersInfo(vm.currentCluster.clusterName);
		});
	}

	function getHadoopClusterNodes(selectedCluster) {
		LOADING_SCREEN();
		prestoSrv.getHadoopClusters(selectedCluster).success(function (data) {
			vm.currentClusterNodes = data.dataNodes;
			var tempArray = [];

			var nameNodeFound = false;
			var jobTrackerFound = false;
			var secondaryNameNodeFound = false;
			for(var i = 0; i < vm.currentClusterNodes.length; i++) {
				var node = vm.currentClusterNodes[i];
				if(node.hostname === data.nameNode.hostname) nameNodeFound = true;
				if(node.hostname === data.jobTracker.hostname) jobTrackerFound = true;
				if(node.hostname === data.secondaryNameNode.hostname) secondaryNameNodeFound = true;
			}
			if(!nameNodeFound) {
				tempArray.push(data.nameNode);
			}
			if(!jobTrackerFound) {
				if(tempArray[0].hostname != data.jobTracker.hostname) {
					tempArray.push(data.jobTracker);
				}
			}
			if(!secondaryNameNodeFound) {
				var checker = 0;
				for(var i = 0; i < tempArray.length; i++) {
					if(tempArray[i].hostname != data.secondaryNameNode.hostname) {
						checker++;
					}
				}
				if(checker == tempArray.length) {
					tempArray.push(data.secondaryNameNode);
				}
			}
			vm.currentClusterNodes = vm.currentClusterNodes.concat(tempArray);

			LOADING_SCREEN('none');
		});
	}

	function createPresto() {
		if(vm.prestoInstall.clusterName === undefined || vm.prestoInstall.clusterName.length == 0) return;
		if(vm.prestoInstall.hadoopClusterName === undefined || vm.prestoInstall.hadoopClusterName.length == 0) return;
		SweetAlert.swal("Success!", "Presto cluster start creating.", "success");
		prestoSrv.createPresto(vm.prestoInstall).success(function (data) {
			SweetAlert.swal("Success!", "Your Presto cluster successfully created.", "success");
			getClusters();
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Presto cluster create error: ' + error, "error");
		});
		setDefaultValues();
		vm.activeTab = 'manage';
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
				prestoSrv.deleteCluster(vm.currentCluster.clusterName).success(function (data) {
					SweetAlert.swal("Deleted!", "Cluster has been deleted.", "success");
					vm.currentCluster = {};
					getClusters();
				}).error(function(data){
					SweetAlert.swal("ERROR!", 'Delete cluster error: ' + data, "error");
				});
			}
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
				prestoSrv.deleteNode(vm.currentCluster.clusterName, nodeId).success(function (data) {
					SweetAlert.swal("Deleted!", "Node has been deleted.", "success");
					getClustersInfo(vm.currentCluster.clusterName);
				});
			}
		});
	}

	function addContainer(containerId) {
		if(vm.prestoInstall.nodes.indexOf(containerId) > -1) {
			vm.prestoInstall.nodes.splice(vm.prestoInstall.nodes.indexOf(containerId), 1);
		} else {
			vm.prestoInstall.nodes.push(containerId);
		}
	}

	function setDefaultValues() {
		vm.prestoInstall = {};
		vm.prestoInstall.nodes = [];
		vm.prestoInstall.server = {};
	}


	function changeDirective(server) {
		vm.otherNodes = [];
		for (var i = 0; i < vm.currentClusterNodes.length; ++i) {
			if (vm.currentClusterNodes[i].uuid !== server) {
				vm.otherNodes.push (vm.currentClusterNodes[i]);
			}
		}
	}

	function startServer() {
		if(vm.currentCluster.clusterName === undefined) return;
		vm.currentCluster.server.status = 'STARTING';
		prestoSrv.startNode (vm.currentCluster.clusterName, vm.currentCluster.server.hostname).success (function (data) {
			SweetAlert.swal("Success!", "Your server started.", "success");
			vm.currentCluster.server.status = 'RUNNING';
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Presto server start error: ' + error, "error");
			vm.currentCluster.server.status = 'ERROR';
		});
	}


	function stopServer() {
		if(vm.currentCluster.clusterName === undefined) return;
		vm.currentCluster.server.status = 'STOPPING';
		prestoSrv.stopNode (vm.currentCluster.clusterName, vm.currentCluster.server.hostname).success (function (data) {
			SweetAlert.swal("Success!", "Your server stopped.", "success");
			vm.currentCluster.server.status = 'STOPPED';
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Presto server stop error: ' + error, "error");
			vm.currentCluster.server.status = 'ERROR';
		});
	}


	function checkIfPushed(id) {
		for (var i = 0; i < vm.nodes2Action.length; ++i) {
			if (vm.nodes2Action[i].name === id) {
				return i;
			}
		}
		return -1;
	}


	function pushNode(id, type) {
		var index = checkIfPushed (id);
		console.log (index);
		if(index !== -1) {
			vm.nodes2Action.splice(index, 1);
		} else {
			vm.nodes2Action.push({name: id, type: type});
		}
	}


	function pushAll() {
		if (vm.currentCluster.coordinator !== undefined) {
			if (vm.nodes2Action.length === vm.currentCluster.workers.length + 1) {
				vm.nodes2Action = [];
			}
			else {
				vm.nodes2Action.push ({name: vm.currentCluster.coordinator.hostname, type: "coordinator"});
				for (var i = 0; i < vm.currentCluster.workers.length; ++i) {
					vm.nodes2Action.push ({name: vm.currentCluster.workers[i].hostname, type: "worker"});
				}
				console.log (vm.nodes2Action);
			}
		}
	}


	function changeClusterScaling (val) {
		prestoSrv.changeClusterScaling (vm.currentCluster.clusterName, val);
	}


	function startNodes() {
		console.log (vm.nodes2Action);
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.clusterName === undefined) return;
		prestoSrv.startNodes(vm.currentCluster.clusterName, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster nodes started successfully.", "success");
			getClustersInfo(vm.currentCluster.name);
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster start error: ' + error.ERROR, "error");
		});
	}


	function stopNodes() {
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.clusterName === undefined) return;
		prestoSrv.stopNodes(vm.currentCluster.clusterName, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster nodes stoped successfully.", "success");
			getClustersInfo(vm.currentCluster.name);
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster stop error: ' + error.ERROR, "error");
		});
	}
}

function colSelectPrestoNodes() {
	return {
		restrict: 'E',
		templateUrl: 'plugins/presto/directives/col-select/col-select-containers.html'
	}
};

