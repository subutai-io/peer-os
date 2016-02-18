'use strict';

angular.module('subutai.accountSettings.controller', [])
	.controller('AccountCtrl', AccountCtrl);

AccountCtrl.$inject = ['identitySrv', '$scope', '$rootScope', 'ngDialog', 'SweetAlert', 'cfpLoadingBar', '$timeout'];

function AccountCtrl(identitySrv, $scope, $rootScope, ngDialog, SweetAlert, cfpLoadingBar, $timeout) {

	var vm = this;

	vm.message = "That's my message!";
	vm.activeUser = {publicKey: ''};

	vm.hasPGPplugin = true;
	$timeout(function() {
		vm.hasPGPplugin = hasPGPplugin();
		if(!vm.hasPGPplugin) {
			$rootScope.notifications = {
				"message": "You dosent have PGP plugin, plese setup you public Key manualy or just install plugin and it will automaticaly add key", 
				"date": moment().format('MMMM Do YYYY, HH:mm:ss'),
				"links": [
					{"text": "PGPlugin", "href": "https://github.com/subutai-io/Tooling-pgp-plugin/releases/latest"},
					{"text": "Set manualy", "href": "/#/account-settings"}
				]
			};
		}
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
			//SweetAlert.swal("Approved!", "Message was successfully approved.", "success");
			$rootScope.notifications = {
				"message": "Message was successfully approved", 
				"date": moment().format('MMMM Do YYYY, HH:mm:ss')
			};
		}).error(function(error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Error: " + error, "error");

			$rootScope.notifications = {
				"message": "Error on approve Document. " + error, 
				"date": moment().format('MMMM Do YYYY, HH:mm:ss')
			};
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

				$rootScope.notifications = {
					"message": "Public key successfully set", 
					"date": moment().format('MMMM Do YYYY, HH:mm:ss')
				};

				getDelegateDocument();
			}).error(function(error) {
				LOADING_SCREEN('none');
				SweetAlert.swal("ERROR!", "Error: " + error, "error");
			});
		}).error(function(error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Error: " + error, "error");

			$rootScope.notifications = {
				"message": "Error on adding Public key. " + error, 
				"date": moment().format('MMMM Do YYYY, HH:mm:ss')
			};
		});
	};
}

