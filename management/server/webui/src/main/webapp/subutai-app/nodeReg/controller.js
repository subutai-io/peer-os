'use strict';

angular.module('subutai.nodeReg.controller', [])
    .controller('NodeRegCtrl', NodeRegCtrl);

NodeRegCtrl.$inject = [ '$scope', 'nodeRegSrv', 'SweetAlert', 'DTOptionsBuilder', 'DTColumnDefBuilder', 'cfpLoadingBar', 'ngDialog'];

function NodeRegCtrl($scope, nodeRegSrv, SweetAlert, DTOptionsBuilder, DTColumnDefBuilder, cfpLoadingBar, ngDialog) {
    var vm = this;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	vm.action = 'install';
	vm.nodes = [];

	//functions
	vm.approve = approve;
	vm.reject = reject;
	vm.remove = remove;
	vm.findIp = findIp;
	vm.editingRh = {};
	vm.changeNamePopup = changeNamePopup;
	vm.setHostName = setHostName;

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

    function findIp(interfaces){
        var i=0, len=interfaces.length;
        for (; i<len; i++) {
          if (interfaces[i].interfaceName == 'wan') {
            return interfaces[i].ip;
          }
        }
        return 'No IP detected';
    }

	function getNodes() {
		nodeRegSrv.getData().success(function(data){
			vm.nodes = data;
		});
	}
	getNodes();


	function approve(nodeId) {

		if(nodeId === undefined) return;

		LOADING_SCREEN();
		nodeRegSrv.approveReq( nodeId ).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Host has been approved",
				"success"
			);
			LOADING_SCREEN('none');
			getNodes();
		}).error(function(error){
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", error, "error");
		});
	}

	function reject(nodeId) {
		if(nodeId === undefined) return;

		LOADING_SCREEN();
		nodeRegSrv.rejectReq( nodeId ).success(function (data) {
			SweetAlert.swal(
				"Success!",
				"Host has been blocked",
				"success"
			);
			LOADING_SCREEN('none');
			getNodes();
		}).error(function(error){
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", error, "error");
		});
	}

	function remove(nodeId, status) {
		if(nodeId === undefined) return;

		LOADING_SCREEN();
		if(status == 'APPROVED'){
            nodeRegSrv.removeReq( nodeId ).success(function (data) {
                SweetAlert.swal(
                    "Success!",
                    "Host has been removed",
                    "success"
                );
                LOADING_SCREEN('none');
                getNodes();
            }).error(function(error){
                LOADING_SCREEN('none');
                SweetAlert.swal("ERROR!", "Oops. Please, try again later", "error");
            });
		}else if(status == 'REJECTED'){
            nodeRegSrv.unblockReq( nodeId ).success(function (data) {
                SweetAlert.swal(
                    "Success!",
                    "Host has been unblocked",
                    "success"
                );
                LOADING_SCREEN('none');
                getNodes();
            }).error(function(error){
                LOADING_SCREEN('none');
                SweetAlert.swal("ERROR!", error, "error");
            });
		}
	}

    function changeNamePopup( rh ) {
        vm.editingRh = rh;

        ngDialog.open({
            template: 'subutai-app/nodeReg/partials/changeName.html',
            scope: $scope,
            className: 'b-build-environment-info'
        });
    }

    function setHostName( rh, name ) {
        LOADING_SCREEN();
        nodeRegSrv.changeHostName( rh.id, name ).success( function (data) {
            location.reload();
        } ).error( function (error) {
            ngDialog.closeAll();
            LOADING_SCREEN('none');
            SweetAlert.swal ("ERROR!", $.trim(error) ? error: 'Invalid hostname', "error");
        } );
    }

};

