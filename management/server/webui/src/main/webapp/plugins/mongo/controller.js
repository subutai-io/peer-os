'use strict';

angular.module('subutai.plugins.mongo.controller', [])
	.controller('MongoCtrl', MongoCtrl)
	.directive('colSelectConfignodes', colSelectConfignodes)
	.directive('colSelectRoutenodes', colSelectRoutenodes)
	.directive ("colSelectDatanodes", colSelectDatanodes);

MongoCtrl.$inject = ['mongoSrv', 'SweetAlert'];
function MongoCtrl(mongoSrv, SweetAlert) {
	var vm = this;
	vm.loading = false;
	vm.activeTab = 'install';//
	vm.clusters = [];//
	vm.environments = [];
	vm.mongoInstall = {};
	vm.configNodes = [];
	vm.routeNodes = [];
	vm.dataNodes = [];

	vm.currentCluster = {};//
	vm.nodes2Action = [];

	//functions
	vm.showContainers = showContainers;
	vm.createMongo = createMongo;
	vm.addConfigNode = addConfigNode;
	vm.addRouteNode = addRouteNode;
	vm.addDataNode = addDataNode;
	vm.getClustersInfo = getClustersInfo;
	vm.startNodes = startNodes;
	vm.stopNodes = stopNodes;
	vm.pushNode = pushNode;
	vm.addNode = addNode;
	vm.deleteNode = deleteNode;
	vm.deleteCluster = deleteCluster;
	vm.changeClusterScaling = changeClusterScaling;
	vm.sendRouter = sendRouter;
	vm.sendDataNode = sendDataNode;


	// Init
	mongoSrv.getEnvironments().success (function (data) {
    	vm.environments = data;
    });

	mongoSrv.listClusters().success(function (data) {
		vm.clusters = data;
	});


	// Install
	function setDefaultValues() {
		vm.mongoInstall.domainName = 'intra.lan';
		vm.mongoInstall.repl = 'repl';
		vm.mongoInstall.configPort = '27019';
		vm.mongoInstall.routePort = '27018';
		vm.mongoInstall.dataPort = '27017';
		vm.mongoInstall.configNodes = [];
		vm.mongoInstall.routeNodes = [];
		vm.mongoInstall.dataNodes = [];
	}
	setDefaultValues();

	function createMongo() {
		console.log (vm.mongoInstall.configNodes.length);
		if (vm.mongoInstall.configNodes.length % 2 !== 1) {
			SweetAlert.swal ("ERROR!", "Number of configuration node servers must be odd");
		}
		else {
			SweetAlert.swal("Success!", "Mongo cluster creating started.", "success");
			mongoSrv.createMongo(JSON.stringify(vm.mongoInstall)).success(function (data) {
				SweetAlert.swal("Success!", "Mongo cluster created.", "success");
			}).error(function (error) {
				SweetAlert.swal("ERROR!", 'Mongo cluster create error: ' + error.ERROR, "error");
			});
		}
	}

	function addConfigNode (containerId) {
		if(vm.mongoInstall.configNodes.indexOf(containerId) > -1) {
			vm.mongoInstall.configNodes.splice(vm.mongoInstall.configNodes.indexOf(containerId), 1);
		} else {
			vm.mongoInstall.configNodes.push(containerId);
		}
	}

	function addRouteNode (containerId) {
		if(vm.mongoInstall.routeNodes.indexOf(containerId) > -1) {
			vm.mongoInstall.routeNodes.splice(vm.mongoInstall.routeNodes.indexOf(containerId), 1);
		} else {
			vm.mongoInstall.routeNodes.push(containerId);
		}
	}


	function addDataNode (containerId) {
		if(vm.mongoInstall.dataNodes.indexOf(containerId) > -1) {
			vm.mongoInstall.dataNodes.splice(vm.mongoInstall.dataNodes.indexOf(containerId), 1);
		} else {
			vm.mongoInstall.dataNodes.push(containerId);
		}
	}

	function showContainers(environmentId) {
		vm.containers = [];
		for(var i in vm.environments) {
			if(environmentId == vm.environments[i].id) {
				for (var j = 0; j < vm.environments[i].containers.length; j++){
					if(vm.environments[i].containers[j].templateName == 'mongo') {
						vm.configNodes.push(vm.environments[i].containers[j]);
						vm.routeNodes.push(vm.environments[i].containers[j]);
						vm.dataNodes.push(vm.environments[i].containers[j]);
					}
				}
				break;
			}
		}
	}

	// Manage

	function getClustersInfo(selectedCluster) {
		vm.loading = true;
		mongoSrv.listClusters(selectedCluster).success(function (data) {
			vm.loading = false;
			vm.currentCluster = data;
		});
	}

	function startNodes() { // TODO
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.clusterName === undefined) return;
		mongoSrv.startNodes(vm.currentCluster.clusterName, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster nodes started successfully.", "success");
			getClustersInfo(vm.currentCluster.clusterName);
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster start error: ' + error.ERROR, "error");
		});
	}


	function stopNodes() { // TODO
		if(vm.nodes2Action.length == 0) return;
		if(vm.currentCluster.clusterName === undefined) return;
		mongoSrv.stopNodes(vm.currentCluster.clusterName, JSON.stringify(vm.nodes2Action)).success(function (data) {
			SweetAlert.swal("Success!", "Your cluster nodes stopped successfully.", "success");
			getClustersInfo(vm.currentCluster.clusterName);
		}).error(function (error) {
			SweetAlert.swal("ERROR!", 'Cluster stop error: ' + error.ERROR, "error");
		});
	}

	function pushNode(id, type) {
		if(vm.nodes2Action.indexOf(id) >= 0) {
			vm.nodes2Action.splice(vm.nodes2Action.indexOf(id), 1);
		} else {
			vm.nodes2Action.push({name: id, type: type});
		}
	}

	function addNode() {
		if(vm.currentCluster.clusterName === undefined) return;
		SweetAlert.swal("Success!", "Adding node action started.", "success");
		mongoSrv.addNode(vm.currentCluster.clusterName).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Node has been added on cluster " + vm.currentCluster.clusterName + ".",
				"success"
			);
			getClustersInfo(vm.currentCluster.clusterName);
		});
	}

	function deleteNode(nodeId, nodeType) { // TODO
		console.log (nodeId);
		console.log (nodeType);
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
					mongoSrv.destroyNode(vm.currentCluster.clusterName, nodeId, nodeType).success(function (data) {
						SweetAlert.swal("Deleted!", "Node has been deleted.", "success");
						getClustersInfo(vm.currentCluster.clusterName);
					});
				}
			}
		);
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
					mongoSrv.destroyCluster(vm.currentCluster.clusterName).success(function (data) {
						SweetAlert.swal("Deleted!", "Cluster has been deleted.", "success");
						vm.currentCluster = {};
					});
				}
			}
		);
	}


	function changeClusterScaling(scale) {
		if(vm.currentCluster.clusterName === undefined) return;
		try {
			mongoSrv.changeClusterScaling(vm.currentCluster.clusterName, scale);
		} catch(e) {}
	}


	function sendRouter() {
		if(vm.currentCluster.clusterName === undefined) return;
		SweetAlert.swal("Success!", "Your Mongo cluster started to add additional router.", "success");
		mongoSrv.sendRouter(vm.currentCluster.clusterName).success (function (data) {
			SweetAlert.swal("Success!", "Router added.", "success");
			getClustersInfo(vm.currentCluster.clusterName);
		}).error (function (error) {
			SweetAlert.swal("ERROR!", 'Cluster error while adding router: ' + error.ERROR, "error");
		});
	}


	function sendDataNode() {
		if(vm.currentCluster.clusterName === undefined) return;
		SweetAlert.swal("Success!", "Mongo cluster started to add additional data node.", "success");
		mongoSrv.sendDataNode(vm.currentCluster.clusterName).success (function (data) {
			SweetAlert.swal("Success!", "Data node added.", "success");
			getClustersInfo(vm.currentCluster.clusterName);
		}).error (function (error) {
			SweetAlert.swal("ERROR!", 'Cluster error while adding data node: ' + error.ERROR, "error");
		});
	}
}

function colSelectConfignodes() {
	return {
		restrict: 'E',
		templateUrl: 'plugins/mongo/directives/col-select/col-select-confignodes.html'
	}
};

function colSelectRoutenodes() {
	return {
		restrict: 'E',
		templateUrl: 'plugins/mongo/directives/col-select/col-select-routenodes.html'
	}
};


function colSelectDatanodes() {
	return {
		restrict: 'E',
		templateUrl: 'plugins/mongo/directives/col-select/col-select-datanodes.html'
	}
};

