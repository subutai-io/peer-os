'use strict';

angular.module('subutai.accountSettings.controller', [])
	.controller('AccountCtrl', AccountCtrl);

AccountCtrl.$inject = ['identitySrv', '$scope', '$rootScope', 'ngDialog', 'SweetAlert', 'cfpLoadingBar', '$timeout'];

function AccountCtrl(identitySrv, $scope, $rootScope, ngDialog, SweetAlert, cfpLoadingBar, $timeout) {

	var vm = this;

	vm.message = "That's my message!";
	vm.activeUser = {publicKey: ''};
	vm.publicKey = '';

	vm.hasPGPplugin = true;
	$timeout(function() {
		vm.hasPGPplugin = hasPGPplugin();
		if(!vm.hasPGPplugin) {
			$rootScope.notifications = {
				"message": "Life is hard when you're stupid dude! Install the subutai browser plugin for added security with end to end encryption.", 
				"date": moment().format('MMMM Do YYYY, HH:mm:ss'),
				"links": [
					{"text": "Take it!", "href": "https://github.com/subutai-io/Tooling-pgp-plugin/releases/latest"},
					{"text": "Set manualy", "href": "/#/account-settings"}
				]
			};
		}
	}, 4000);

	cfpLoadingBar.start();
	angular.element(document).ready(function () {
		cfpLoadingBar.complete();
	});

	//functions
	vm.approveDocument = approveDocument;
	vm.autoSign = autoSign;
	vm.setPublicKey = setPublicKey;

	identitySrv.getCurrentUser().success(function (data)
	{
		vm.activeUser = data;

		identitySrv.getPublicKeyData(vm.activeUser.id).success(function (data) {
			vm.publicKeyInfo = data;
		});

		identitySrv.checkUserKey(vm.activeUser.id).success(function (data) {
			if(data <= 1 && $rootScope.$state.current.name != 'account-settings') {
				$('.js-auto-set-key').addClass('bp-set-pub-key');
			} /*else {
				$('.js-public-key-manager').remove();
			}*/
		});
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

	function setPublicKey(publicKey) {
		LOADING_SCREEN();
		$('#js-public-key-manager').removeClass('js-public-key-manager_show');
		identitySrv.updatePublicKey(encodeURIComponent(publicKey)).success(function(data) {
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
