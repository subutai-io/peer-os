'use strict';

angular.module('subutai.plugins.spark.controller', [])
    .controller('SparkCtrl', SparkCtrl)
	.directive('colSelectSparkNodes', colSelectSparkNodes);

SparkCtrl.$inject = ['$scope', 'sparkSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', 'ngDialog'];

function SparkCtrl($scope, sparkSrv, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, ngDialog) {
    var vm = this;
	vm.activeTab = 'install';
	vm.sparkInstall = {};
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
	vm.createSpark = createSpark;
	vm.deleteNode = deleteNode;
	vm.addNodeForm = addNodeForm;
	vm.addNode = addNode;
	vm.deleteCluster = deleteCluster;
	vm.changeDirective = changeDirective;
	vm.startMaster = startMaster;
	vm.stopMaster = stopMaster;
	vm.pushNode = pushNode;
	vm.startNodes = startNodes;
	vm.stopNodes = stopNodes;

	sparkSrv.getHadoopClusters().success(function(data){
		vm.hadoopClusters = data;
		if(vm.hadoopClusters.length == 0) {
			SweetAlert.swal("ERROR!", 'No Hadoop clusters was found! Create Hadoop cluster first.', "error");
		}
	}).error(function(data){
		SweetAlert.swal("ERROR!", 'No Hadoop clusters was found! ERROR: ' + data, "error");
	});
	setDefaultValues();

	function getClusters() {
		sparkSrv.getClusters().success(function (data) {
			vm.clusters = data;
		});
	}
	getClusters();

	function pushNode(id) {
		if(vm.nodes2Action.indexOf(id) >= 0) {
			vm.nodes2Action.splice(vm.nodes2Action.indexOf(id), 1);
		} else {
			vm.nodes2Action.push(id);
		}
	}

	function startNodes() {
		console.log(vm.nodes2Action);
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.clusterName === undefined) return;
		sparkSrv.startNodes(vm.currentCluster.clusterName, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster slaves started successfully.", "success");
			getClustersInfo(vm.currentCluster.clusterName);
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster slaves start error: ' + error, "error");
		});
	}

	function stopNodes() {
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.clusterName === undefined) return;
		sparkSrv.stopNodes(vm.currentCluster.clusterName, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster slaves stoped successfully.", "success");
			getClustersInfo(vm.currentCluster.clusterName);
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster slaves stop error: ' + error, "error");
		});
	}

	vm.dtOptions = DTOptionsBuilder
		.newOptions()
		.withOption('order', [[0, "asc" ]])
		.withOption('stateSave', true)
		.withPaginationType('full_numbers');

	vm.dtColumnDefs = [
		DTColumnDefBuilder.newColumnDef(0).notSortable(),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2),
		DTColumnDefBuilder.newColumnDef(3),
		DTColumnDefBuilder.newColumnDef(4).notSortable()
	];

	function getClustersInfo(selectedCluster) {
		LOADING_SCREEN();
		sparkSrv.getClusters(selectedCluster).success(function (data) {
			vm.currentCluster = data;
			LOADING_SCREEN('none');
		});
	}

	function addNodeForm() {
		if(vm.currentCluster.clusterName === undefined) return;
		sparkSrv.getAvailableNodes(vm.currentCluster.clusterName).success(function (data) {
			vm.availableNodes = data;
		});
		ngDialog.open({
			template: 'plugins/spark/partials/addNodesForm.html',
			scope: $scope
		});
	}

	function addNode(chosenNode) {
		if(chosenNode === undefined) return;
		if(vm.currentCluster.clusterName === undefined) return;
		SweetAlert.swal("Success!", "Adding node action started.", "success");
		ngDialog.closeAll();
		sparkSrv.addNode(vm.currentCluster.clusterName, chosenNode).success(function (data) {
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
		sparkSrv.getHadoopClusters(selectedCluster).success(function (data) {
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

	function createSpark() {
		if(vm.sparkInstall.clusterName === undefined || vm.sparkInstall.clusterName.length == 0) return;
		if(vm.sparkInstall.hadoopClusterName === undefined || vm.sparkInstall.hadoopClusterName.length == 0) return;
		SweetAlert.swal("Success!", "Spark cluster start creating.", "success");
		sparkSrv.createSpark(vm.sparkInstall).success(function (data) {
			SweetAlert.swal("Success!", "Your Spark cluster start creating.", "success");
			getClusters();
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Spark cluster create error: ' + error, "error");
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
				sparkSrv.deleteCluster(vm.currentCluster.clusterName).success(function (data) {
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
				sparkSrv.deleteNode(vm.currentCluster.clusterName, nodeId).success(function (data) {
					SweetAlert.swal("Deleted!", "Node has been deleted.", "success");
					getClustersInfo(vm.currentCluster.clusterName);
				}).error(function(error){
					SweetAlert.swal("ERROR!", 'Delete cluster node error: ' + error, "error");
				});
			}
		});
	}

	function addContainer(containerId) {
		if(vm.sparkInstall.nodes.indexOf(containerId) > -1) {
			vm.sparkInstall.nodes.splice(vm.sparkInstall.nodes.indexOf(containerId), 1);
		} else {
			vm.sparkInstall.nodes.push(containerId);
		}
	}

	function setDefaultValues() {
		vm.sparkInstall = {};
		vm.sparkInstall.nodes = [];
		vm.sparkInstall.server = {};
		vm.otherNodes = [];
	}


	function changeDirective(server) {
		vm.otherNodes = [];
		for (var i = 0; i < vm.currentClusterNodes.length; ++i) {
			if (vm.currentClusterNodes[i].uuid !== server) {
				vm.otherNodes.push (vm.currentClusterNodes[i]);
			}
		}
	}

	function startMaster() {
		if(vm.currentCluster.clusterName === undefined) return;
		vm.currentCluster.server.status = 'STARTING';
		sparkSrv.startMasterNode(vm.currentCluster.clusterName, vm.currentCluster.server.hostname).success (function (data) {
			SweetAlert.swal("Success!", "Your server started.", "success");
			vm.currentCluster.server.status = 'RUNNING';
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Spark server start error: ' + error, "error");
			vm.currentCluster.server.status = 'ERROR';
		});
	}


	function stopMaster() {
		if(vm.currentCluster.clusterName === undefined) return;
		vm.currentCluster.server.status = 'STOPPING';
		sparkSrv.stopMasterNode(vm.currentCluster.clusterName, vm.currentCluster.server.hostname).success (function (data) {
			SweetAlert.swal("Success!", "Your server stopped.", "success");
			vm.currentCluster.server.status = 'STOPPED';
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Spark server stop error: ' + error, "error");
			vm.currentCluster.server.status = 'ERROR';
		});
	}
}

function colSelectSparkNodes() {
	return {
		restrict: 'E',
		templateUrl: 'plugins/spark/directives/col-select/col-select-containers.html'
	}
};

