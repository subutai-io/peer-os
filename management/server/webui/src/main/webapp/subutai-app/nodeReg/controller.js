'use strict';

angular.module('subutai.nodeReg.controller', [])
    .controller('NodeRegCtrl', NodeRegCtrl);

NodeRegCtrl.$inject = [ 'nodeRegSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', 'cfpLoadingBar'];

function NodeRegCtrl(nodeRegSrv, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, cfpLoadingBar) {
    var vm = this;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.action = 'install';
	vm.nodes = [];

	//functions
	vm.approveNode = approveNode;

	vm.dtOptions = DTOptionsBuilder
			.newOptions()
			.withOption('order', [[1, "asc" ]])
			.withOption('stateSave', true)
			.withPaginationType('full_numbers');

	vm.dtColumnDefs = [
		DTColumnDefBuilder.newColumnDef(0).notSortable(),
		DTColumnDefBuilder.newColumnDef(1),
		DTColumnDefBuilder.newColumnDef(2)
	];


	function getNodes() {
		nodeRegSrv.getData().success(function(data){
			vm.nodes = data;
		});
	}
	getNodes();


	function approveNode(nodeId) {

		if(nodeId === undefined) return;

		SweetAlert.swal("Success!", "Node is being approved.", "success");

		LOADING_SCREEN();
		nodeRegSrv.approveNode( nodeId ).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Node has been added to cluster.",
				"success"
			);
			LOADING_SCREEN('none');
			getNodes();
		}).error(function(error){
			SweetAlert.swal("ERROR!", error, "error");
			LOADING_SCREEN('none');
		});
	}
};

