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
	vm.unreg = unreg;
	vm.remove = remove;
	vm.checkManagement = checkManagement;

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

	function unreg(nodeId) {
		if(nodeId === undefined) return;

		LOADING_SCREEN();
		nodeRegSrv.unregNode( nodeId ).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Node has been unregistered.",
				"success"
			);
			LOADING_SCREEN('none');
			getNodes();
		}).error(function(error){
			SweetAlert.swal("ERROR!", error, "error");
			LOADING_SCREEN('none');
		});
	}

	function remove(nodeId) {
		if(nodeId === undefined) return;

		LOADING_SCREEN();
		nodeRegSrv.removeReq( nodeId ).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Node has been unregistered.",
				"success"
			);
			LOADING_SCREEN('none');
			getNodes();
		}).error(function(error){
			SweetAlert.swal("ERROR!", error, "error");
			LOADING_SCREEN('none');
		});
	}

	function checkManagement( hostInfo ) {
		console.log( hostInfo );
		for( var i = 0; i < hostInfo.length; i++ )
		{
			if( hostInfo[i].hostname == "management" )
				return true;
		}
		return false;
	}
};

