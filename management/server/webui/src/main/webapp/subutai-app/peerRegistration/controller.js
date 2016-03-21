'use strict';

angular.module('subutai.peer-registration.controller', [])
	.controller('PeerRegistrationCtrl', PeerRegistrationCtrl)
	.controller('PeerRegistrationPopupCtrl', PeerRegistrationPopupCtrl);

PeerRegistrationCtrl.$inject = ['$scope', 'peerRegistrationService', 'objectRelationService', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'SweetAlert', 'ngDialog', 'cfpLoadingBar'];
PeerRegistrationPopupCtrl.$inject = ['$scope', 'peerRegistrationService', 'objectRelationService', 'ngDialog', 'SweetAlert'];

function PeerRegistrationCtrl($scope, peerRegistrationService, objectRelationService, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, SweetAlert, ngDialog, cfpLoadingBar) {

	var vm = this;

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	// functions
	vm.peerFrom = peerFrom;
	vm.rejectPeerRequest = rejectPeerRequest;
	vm.approvePeerRequest = approvePeerRequest;
	vm.unregisterPeer = unregisterPeer;
	vm.cancelPeerRequest = cancelPeerRequest;
	vm.issueRelationChallenge = issueRelationChallenge;

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
		if(data.isOnline == false) {
			status = 'false';
			statusText = 'offline';
		}
		return '<div class="b-status-icon b-status-icon_' + status + '" title="' + statusText + '"></div>';
	}

	function actionButton(data, type, full, meta) {
		var result = '';
		var challengeStorage = '';
		if(data.registrationData.status == 'APPROVED') {
			challengeStorage = '<div style="display: none">' +
				'<textarea class="b-form-input b-form-input_full b-form-input_textarea bp-sign-input bp-sign-challenge"' +
				'data-challenge="'+data.registrationData.peerInfo.id +'"' +
				'ng-model="data.operationChallenge"' +
				'ng-change="peerRegistrationCtrl.unregisterPeer(\'' + data.registrationData.peerInfo.id + '\', $event)"' +
				'style="width: 420px; height: 160px; font-family: monospace;">' +
				'</textarea></div>';

			result += '<a href class="b-btn b-btn_red subt_button__peer-unregister" ' +
				'ng-click="peerRegistrationCtrl.issueRelationChallenge($event)">' +
				'Unregister' +
					challengeStorage +
				'</a>';
		} else if(data.registrationData.status == 'WAIT') {
			challengeStorage = '<div style="display: none">' +
				'<textarea class="b-form-input b-form-input_full b-form-input_textarea bp-sign-input bp-sign-challenge"' +
				'data-challenge="'+data.registrationData.peerInfo.id +'"' +
				'ng-model="data.operationChallenge"' +
				'ng-change="peerRegistrationCtrl.cancelPeerRequest(\'' + data.registrationData.peerInfo.id + '\')"' +
				'style="width: 420px; height: 160px; font-family: monospace;">' +
				'</textarea>' +
				'</div>';

			result += '<a href class="b-btn b-btn_blue subt_button__peer-cancel" ' +
				'ng-click="peerRegistrationCtrl.issueRelationChallenge($event)">' +
				'Cancel' +
					challengeStorage +
				'</a>';
		} else if(data.registrationData.status == 'REQUESTED') {

			result += '<a href class="b-btn b-btn_green subt_button__peer-approve" ' +
				'ng-click="peerRegistrationCtrl.approvePeerRequest(\'' + data.registrationData.peerInfo.id + '\')"">' +
				'Approve' +
				'</a>';

			var challengeStorage1 = '<div style="display: none">' +
				'<textarea class="b-form-input b-form-input_full b-form-input_textarea bp-sign-input bp-sign-challenge"' +
				'data-challenge="'+data.registrationData.peerInfo.id +'"' +
				'ng-model="data.operationChallenge"' +
				'ng-change="peerRegistrationCtrl.rejectPeerRequest(\'' + data.registrationData.peerInfo.id + '\', $event)"' +
				'style="width: 420px; height: 160px; font-family: monospace;">' +
				'</textarea></div>';
			result += '<a href class="b-btn b-btn_red subt_button__peer-reject" ' +
				'ng-click="peerRegistrationCtrl.issueRelationChallenge($event)">' +
				'Reject' +
					challengeStorage1+
				'</a>';
		}
		return result;
	}

	function issueRelationChallenge($event) {
		objectRelationService.issueChallenge().success(function (data) {
			var storage = $($event.target).find('.bp-sign-input.bp-sign-challenge');
			$(storage).val(data);
			$(storage).addClass('bp-sign-target');

		}).error(function (err) {
			SweetAlert.swal("ERROR!", "Couldn't issue operation challenge token: " + err, "error");
		});
	}

	function peerFrom() {
		ngDialog.open({
			template: 'subutai-app/peerRegistration/partials/peerForm.html',
			controller: 'PeerRegistrationPopupCtrl',
			controllerAs: 'peerRegistrationPopupCtrl',
			preCloseCallback: function(value) {
				if(Object.keys(vm.dtInstance).length !== 0) {
					vm.dtInstance.reloadData(null, false);
				}
			}
		});
	}

	function rejectPeerRequest(peerId) {
		var signedChallenge = $('.bp-sign-target[data-challenge="'+peerId+'"]').val();
		$(signedChallenge).removeClass('bp-sign-target');
		var putData = "peerId=" + peerId + '&challenge=' + encodeURIComponent(signedChallenge);
		peerRegistrationService.rejectPeerRequest(putData).success(function (data) {
			vm.dtInstance.reloadData(null, false);
		}).error(function(error){
			if(error.ERROR !== undefined) {
				SweetAlert.swal("ERROR!", error.ERROR, "error");
			} else {
				SweetAlert.swal("ERROR!", error, "error");
			}
		});
	}

	function approvePeerRequest(peerId) {
		ngDialog.open({
			template: 'subutai-app/peerRegistration/partials/peerApprove.html',
			controller: 'PeerRegistrationPopupCtrl',
			controllerAs: 'peerRegistrationPopupCtrl',
			data: {"peerId": peerId},
			preCloseCallback: function(value) {
				vm.dtInstance.reloadData(null, false);
			}
		});
	}

	function unregisterPeer(peerId) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your unregister peer request!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Unregister",
			cancelButtonText: "Cancel",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {
				var signedChallenge = $('.bp-sign-target[data-challenge="'+peerId+'"]').val();
				$(signedChallenge).removeClass('bp-sign-target');
				var putData = "peerId=" + peerId + '&challenge=' + encodeURIComponent(signedChallenge);
				peerRegistrationService.unregisterPeerRequest(putData).success(function (data) {
					SweetAlert.swal("Unregistered!", "Your peer request has been unregistered.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (error) {
					SweetAlert.swal("ERROR!", error, "error");
				});
			}
		});
	}

	function cancelPeerRequest(peerId) {
		SweetAlert.swal({
			title: "Are you sure?",
			text: "Your cancel peer request!",
			type: "warning",
			showCancelButton: true,
			confirmButtonColor: "#ff3f3c",
			confirmButtonText: "Cancel request",
			cancelButtonText: "No",
			closeOnConfirm: false,
			closeOnCancel: true,
			showLoaderOnConfirm: true
		},
		function (isConfirm) {
			if (isConfirm) {
				var signedChallenge = $('.bp-sign-target[data-challenge="'+peerId+'"]').val();
				$(signedChallenge).removeClass('bp-sign-target');
				var postData = "peerId=" + peerId + '&challenge=' + encodeURIComponent(signedChallenge);
				peerRegistrationService.cancelPeerRequest(postData).success(function (data) {
					SweetAlert.swal("Canceled!", "Your peer request has been canceled.", "success");
					vm.dtInstance.reloadData(null, false);
				}).error(function (error) {
					SweetAlert.swal("ERROR!", error, "error");
				});
			}
		});
	}	

}

function PeerRegistrationPopupCtrl($scope, peerRegistrationService, objectRelationService, ngDialog, SweetAlert) {

	var vm = this;
	vm.peerId = null;
	vm.operationChallenge = null;
	vm.challengeSigned = null;
	vm.allowSubmit= allowSubmit;

	if ($scope.ngDialogData !== undefined) {
		vm.peerId = $scope.ngDialogData.peerId;
	}

	objectRelationService.issueChallenge().success(function (data) {
		vm.operationChallenge = data;
		$('.bp-sign-input.bp-sign-challenge[data-challenge="approve-register"]').addClass('bp-sign-target');
	}).error(function (err) {
		SweetAlert.swal("ERROR!", "Couldn't issue operation challenge token: " + err, "error");
	});

	vm.addPeer = addPeer;
	vm.approvePeerRequest = approvePeerRequest;

	function addPeer(newPeer) {
		var postData = 'ip=' + newPeer.ip + '&key_phrase=' + newPeer.keyphrase + '&challenge=' + encodeURIComponent(vm.operationChallenge);
		peerRegistrationService.registerRequest(postData).success(function (data) {
			ngDialog.closeAll();
		}).error(function (error) {
			SweetAlert.swal("ERROR!", "Peer request error: " + error, "error");
		});
	}

	function approvePeerRequest(keyPhrase) {
		peerRegistrationService.approvePeerRequest(vm.peerId, keyPhrase, vm.operationChallenge).success(function (data) {
			ngDialog.closeAll();
		}).error(function (error) {
			SweetAlert.swal("ERROR!", "Peer approve error: " + error, "error");
		});
	}

	function allowSubmit() {
		$('.bp-sign-input.bp-sign-challenge[data-challenge="approve-register"]').removeClass('bp-sign-target');
		vm.challengeSigned = true;
	}
}

