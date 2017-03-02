'use strict';

angular.module('subutai.peer-registration.controller', [])
	.controller('PeerRegistrationCtrl', PeerRegistrationCtrl);

PeerRegistrationCtrl.$inject = ['$scope', 'peerRegistrationService', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'SweetAlert', 'ngDialog', 'cfpLoadingBar'];

function PeerRegistrationCtrl($scope, peerRegistrationService, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, SweetAlert, ngDialog, cfpLoadingBar) {

	var vm = this;
	vm.peerId = null;
	vm.peerToAction = '';
	vm.action = '';
	vm.peerStatusIco = "";
	vm.editingPeerId = "";
	vm.editingPeerName = "";
	vm.editingPeerIp = "";


	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	// functions
	vm.peerFrom = peerFrom;
	vm.rejectPeerRequest = rejectPeerRequest;
	vm.approvePeerRequestForm = approvePeerRequestForm;
	vm.unregisterPeer = unregisterPeer;
	vm.cancelPeerRequest = cancelPeerRequest;
	vm.addPeer = addPeer;
	vm.approvePeerRequest = approvePeerRequest;
	vm.confirmPopup = confirmPopup;
	vm.checkResourceHost = checkResourceHost;
	vm.changeNamePopup = changeNamePopup;
	vm.renamePeer = renamePeer;
	vm.updatePeerUrl = updatePeerUrl;
	vm.updateUrlPopup = updateUrlPopup;

	vm.dtInstance = {};
	vm.users = {};
	vm.dtOptions = DTOptionsBuilder
		.fromFnPromise(function() {
			return $resource( peerRegistrationService.getPeersUrl() ).query().$promise;
		})
		.withPaginationType('full_numbers')
		.withOption('createdRow', createdRow)
		.withOption('columnDefs', [
			{className: "b-main-table__buttons-group", "targets": 3},
			{className: "b-main-table__peer-status-col", "targets": 2}
		])
		.withOption('stateSave', true);

	vm.dtColumns = [
		DTColumnBuilder.newColumn('registrationData.peerInfo.name').withTitle('Name'),
		DTColumnBuilder.newColumn('registrationData.peerInfo.ip').withTitle(' IP'),
		DTColumnBuilder.newColumn(null).withTitle('Status').renderWith(statusHTML),
		DTColumnBuilder.newColumn(null).withTitle('').notSortable().renderWith(actionButton),
	];

	function createdRow(row, data, dataIndex) {
		$compile(angular.element(row).contents())($scope);
	}

	function statusHTML(data, type, full, meta) {
		var status = data.registrationData.status;
		var statusText = data.registrationData.status;

		if( data.registrationData.status == "APPROVED" )
		{
			if(data.isOnline == false)
			{
				status = 'false';
				statusText = 'OFFLINE';
			}
			else
			{
				status = 'true';
				statusText = 'ONLINE';
			}
		}
		return '<div class="b-status-icon b-status-icon_' + status + '" title="' + statusText + '"></div>';
	}

	function actionButton(data, type, full, meta) {
		var result = '';
		if(data.registrationData.status == 'APPROVED') {
			result += '<a href class="b-btn b-btn_red subt_button__peer-unregister" ng-click="peerRegistrationCtrl.confirmPopup(\'' + data.registrationData.peerInfo.id + '\', \'Unregister\')">Unregister</a>';
			result += '<a href class="b-btn b-btn_blue subt_button__peer-rename" ng-click="peerRegistrationCtrl.changeNamePopup(\'' + data.registrationData.peerInfo.id + '\', \'' + data.registrationData.peerInfo.name + '\')">Rename</a>';
			result += '<a href class="b-btn b-btn_white subt_button__peer-update-url" ng-click="peerRegistrationCtrl.updateUrlPopup(\'' + data.registrationData.peerInfo.id + '\', \'' + data.registrationData.peerInfo.publicUrl + '\')">Change url</a>';
		} else if(data.registrationData.status == 'WAIT') {
			//result += '<a href class="b-btn b-btn_blue subt_button__peer-cancel" ng-click="peerRegistrationCtrl.cancelPeerRequest(\'' + data.registrationData.peerInfo.id + '\')">Cancel</a>';
			result += '<a href class="b-btn b-btn_blue subt_button__peer-cancel" ng-click="peerRegistrationCtrl.confirmPopup(\'' + data.registrationData.peerInfo.id + '\', \'Cancel\')">Cancel</a>';
		} else if(data.registrationData.status == 'REQUESTED') {
			result += '<a href class="b-btn b-btn_green subt_button__peer-approve" ng-click="peerRegistrationCtrl.approvePeerRequestForm(\'' + data.registrationData.peerInfo.id + '\')">Approve</a>';
			//result += '<a href class="b-btn b-btn_red subt_button__peer-reject" ng-click="peerRegistrationCtrl.rejectPeerRequest(\'' + data.registrationData.peerInfo.id + '\')">Reject</a>';
			result += '<a href class="b-btn b-btn_red subt_button__peer-reject" ng-click="peerRegistrationCtrl.confirmPopup(\'' + data.registrationData.peerInfo.id + '\', \'Reject\')">Reject</a>';
		}

		return result;
	}

	function rejectPeerRequest(peerId, force) {
		ngDialog.closeAll();
		LOADING_SCREEN();
		peerRegistrationService.rejectPeerRequest(peerId, force).success(function (data) {
			LOADING_SCREEN('none');
			vm.dtInstance.reloadData(null, false);
		}).error(function(error){
			LOADING_SCREEN('none');
			if(error.ERROR !== undefined) {
				SweetAlert.swal("ERROR!", error.ERROR, "error");
			} else {
				SweetAlert.swal("ERROR!", error, "error");
			}
		});
	}

	function peerFrom() {
		vm.peerStatusIco = '';
		ngDialog.open({
			template: 'subutai-app/peerRegistration/partials/peerForm.html',
			scope: $scope
		});
	}

	function approvePeerRequestForm(peerId) {
		vm.peerId = peerId;
		ngDialog.open({
			template: 'subutai-app/peerRegistration/partials/peerApprove.html',
			scope: $scope
		});
	}

	function addPeer(newPeer) {
		var postData = 'ip=' + newPeer.ip + '&key_phrase=' + newPeer.keyphrase;
		ngDialog.closeAll();
		LOADING_SCREEN();
		peerRegistrationService.registerRequest(postData).success(function (data) {
			LOADING_SCREEN('none');
			if(Object.keys(vm.dtInstance).length !== 0) {
				vm.dtInstance.reloadData(null, false);
			}
		}).error(function(error){
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Peer request error: " + error, "error");
		});
	}

	function approvePeerRequest(keyPhrase) {
		ngDialog.closeAll();
		LOADING_SCREEN();
		peerRegistrationService.approvePeerRequest(vm.peerId, keyPhrase).success(function (data) {
			LOADING_SCREEN('none');
			if(Object.keys(vm.dtInstance).length !== 0) {
				vm.dtInstance.reloadData(null, false);
			}
		}).error(function(error){
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Peer approve error: " + error, "error");
		});
	}

	function confirmPopup(peerId, action) {
		vm.peerToAction = peerId;
		vm.action = action;
		ngDialog.open({
			template: 'subutai-app/peerRegistration/partials/confirmPopup.html',
			scope: $scope
		});
	}

	function unregisterPeer(peerId, force) {
		ngDialog.closeAll();
		LOADING_SCREEN();
		peerRegistrationService.unregisterPeerRequest(peerId, force).success(function (data) {
			SweetAlert.swal("Unregistered!", "Your peer request has been unregistered.", "success");
			LOADING_SCREEN('none');
			vm.dtInstance.reloadData(null, false);
		}).error(function (error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", error, "error");
		});
	}

	function checkResourceHost( info ) {

		if( info.ip )
		{
			LOADING_SCREEN();
			peerRegistrationService.checkPeer( info.ip )
				.success( function (data) {
					vm.peerStatusIco = "check";
					LOADING_SCREEN("none");
				})
				.error( function (data) {
					vm.peerStatusIco = "times";
					LOADING_SCREEN("none");

					SweetAlert.swal({
						title: "ERROR!",
						text: data,
						type: "error",
						confirmButtonColor: "#ff3f3c"
					});
				});
		}
	}

	function cancelPeerRequest(peerId, force) {
		ngDialog.closeAll();
		LOADING_SCREEN();
		peerRegistrationService.cancelPeerRequest(peerId, force).success(function (data) {
			SweetAlert.swal("Canceled!", "Your peer request has been canceled.", "success");
			LOADING_SCREEN('none');
			vm.dtInstance.reloadData(null, false);
		}).error(function (error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", error, "error");
		});
	}

    function renamePeer( peerId, newName ) {
        LOADING_SCREEN();
        peerRegistrationService.renamePeer( peerId, newName ).success( function (data) {
            location.reload();
        } ).error( function (data) {
            SweetAlert.swal ("ERROR!", data);
        } );
    }

    function updatePeerUrl(peerId, ip){
        LOADING_SCREEN();
        peerRegistrationService.updatePeerUrl( peerId, ip ).success( function (data) {
          location.reload();
        } ).error( function (data) {
          SweetAlert.swal ("ERROR!", data);
        } );
    }

	function changeNamePopup( peerId, oldName ) {

		vm.editingPeerId = peerId;
        vm.editingPeerName = oldName;

		ngDialog.open({
			template: 'subutai-app/peerRegistration/partials/changeName.html',
			scope: $scope,
			className: 'b-build-environment-info'
		});
	}

	function updateUrlPopup( peerId, oldIp ) {

		vm.editingPeerId = peerId;
        vm.editingPeerIp = oldIp;

		ngDialog.open({
			template: 'subutai-app/peerRegistration/partials/updateUrl.html',
			scope: $scope,
			className: 'b-build-environment-info'
		});
	}
}

