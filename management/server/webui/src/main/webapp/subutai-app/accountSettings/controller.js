'use strict';

angular.module('subutai.accountSettings.controller', [])
	.controller('AccountCtrl', AccountCtrl);

AccountCtrl.$inject = ['identitySrv', '$scope', 'ngDialog', 'SweetAlert', 'cfpLoadingBar', '$timeout'];

function AccountCtrl(identitySrv, $scope, ngDialog, SweetAlert, cfpLoadingBar, $timeout) {

	var vm = this;

	vm.message = "That's my message!";
	vm.activeUser = {publicKey: ''};

	vm.hasPGPplugin = true;
	$timeout(function() {
		vm.hasPGPplugin = hasPGPplugin();
	}, 2000);

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	//functions
	vm.approveDocument = approveDocument;
	vm.autoSign = autoSign;
	vm.setPublicKey = setPublicKey;

	identitySrv.getCurrentUser().success(function (data) {
		vm.activeUser = data;
		identitySrv.getKey(vm.activeUser.securityKeyId).success(function (key) {
			vm.activeUser.publicKey = key;
			if(key.length == 0 && hasPGPplugin()) {
				$('.js-auto-set-key').addClass('bp-set-pub-key');
			}
		});

		identitySrv.getPublicKeyData(vm.activeUser.id).success(function (data) {
			vm.publicKeyInfo = data;
		});
	});

	identitySrv.getTokenTypes().success(function (data) {
		vm.tokensType = data;
	});

	function getDelegateDocument() {
		identitySrv.getIdentityDelegateDocument().success(function(data) {
			vm.message = data;
			if (vm.message) {
				$('.bp-sign-input').addClass('bp-sign-target');
			}
		});
	}
	//getDelegateDocument();

	function approveDocument() {
		LOADING_SCREEN();
		identitySrv.approveIdentityDelegate(encodeURIComponent(vm.message)).success(function() {
			LOADING_SCREEN('none');
			SweetAlert.swal("Approved!", "Message was successfully approved.", "success");
		}).error(function(error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Error: " + error, "error");
		});
	};

	function autoSign() {
		if(vm.hasPGPplugin) {
			approveDocument();
		}
	}

	function setPublicKey() {
		LOADING_SCREEN();
		$('#js-public-key-manager').removeClass('js-public-key-manager_show');
		identitySrv.updatePublicKey(encodeURIComponent(vm.activeUser.publicKey)).success(function(data) {
			identitySrv.createIdentityDelegateDocument().success(function() {
				LOADING_SCREEN('none');
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
}

