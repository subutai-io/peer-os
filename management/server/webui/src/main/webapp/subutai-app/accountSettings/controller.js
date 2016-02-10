'use strict';

angular.module('subutai.accountSettings.controller', [])
.controller('AccountCtrl', AccountCtrl);


AccountCtrl.$inject = ['identitySrv', '$scope', 'ngDialog', 'SweetAlert', 'cfpLoadingBar'];


function AccountCtrl(identitySrv, $scope, ngDialog, SweetAlert, cfpLoadingBar) {

	var vm = this;

	vm.message = "That's my message!";
	vm.activeUser = {publicKey: ''};

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	function getDelegateDocument() {
		identitySrv.getIdentityDelegateDocument().success(function(data) {
			vm.message = data;
			if (vm.message) {
				$('.bp-sign-input').addClass('bp-sign-target');
			}
		});
	}
	getDelegateDocument();

	vm.approveDocument = function () {
		LOADING_SCREEN();
		identitySrv.approveIdentityDelegate(encodeURIComponent(vm.message)).success(function() {
			LOADING_SCREEN('none');
			SweetAlert.swal("Approved!", "Message was successfully approved.", "success");
		}).error(function(error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Error: " + error, "error");
		});
	};

	vm.setPublicKey = function () {
		LOADING_SCREEN();
		identitySrv.updatePublicKey(encodeURIComponent(vm.activeUser.publicKey)).success(function(data) {
			identitySrv.createIdentityDelegateDocument().success(function() {
				getDelegateDocument();
			}).error(function(error) {
				LOADING_SCREEN('none');
				SweetAlert.swal("ERROR!", "Error: " + error, "error");
			});
		}).error(function(error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Error: " + error, "error");
		});
	};

	identitySrv.getCurrentUser().success(function (data) {
		vm.activeUser = data;
		identitySrv.getKey(vm.activeUser.securityKeyId).success(function (key) {
			vm.activeUser.publicKey = key;
		});
	});

	identitySrv.getTokenTypes().success(function (data) {
		vm.tokensType = data;
	});
}

