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
	$timeout(function () {
		vm.hasPGPplugin = hasPGPplugin();
		if (!vm.hasPGPplugin) {
			var pluginUrl = 'https://github.com/subutai-io/browsers/releases/tag/2.0.13';
			var isFirefox = typeof InstallTrigger !== 'undefined';
			var isChrome = !!window.chrome && !!window.chrome.webstore;

			if (isChrome) {
				pluginUrl = 'https://chrome.google.com/webstore/detail/subutai-social-e2e-plugin/kpmiofpmlciacjblommkcinncmneeoaa?utm_source=chrome-ntp-icon';
			} else if (isFirefox) {
				pluginUrl = 'https://addons.mozilla.org/en-US/firefox/addon/subutai-social-e2e-plugin/';
			}

			$rootScope.notifications = {
				"message": "Install the subutai browser plugin for added security with end to end encryption.",
				"browserPluginMessage": true,
				"date": moment().format('MMMM Do YYYY, HH:mm:ss'),
				"links": [
				{
					"text": "Install",
					"href": pluginUrl,
					"newTab": true
				},
				{
					"text": "Setup Manually",
					"href": "/#/account-settings",
					"tooltip": "Set PGP key manually without plugin"
				}
				]
			};
		} else {

			var notifications = sessionStorage.getItem('notifications');
			if (
					notifications !== null &&
					notifications !== undefined &&
					notifications !== 'null' &&
					notifications.length > 0
			) {
				notifications = JSON.parse(notifications);
				for (var i = 0; i < notifications.length; i++) {
					if (notifications[i].browserPluginMessage !== undefined && notifications[i].browserPluginMessage) {
						notifications.splice(i, 1);
						sessionStorage.setItem('notifications', JSON.stringify(notifications));
						$rootScope.notifications = {};
						break;
					}
				}
			}

		}
	}, 3000);


	identitySrv.isAdminCheck().success(function (data) {
		if (data == true || data == 'true') {
			identitySrv.isUpdateInProgress().success(function (data){
				console.log("Update in progress: " + data);
				 if (data == true || data == 'true') {
					removeUpdateMessage();
				 }else{
					checkUpdate();
				 }
			});
		}
	});

	function checkUpdate() {
		identitySrv.getConfig().success(function (data) {
			if (data.isUpdatesAvailable) {
				$rootScope.notifications = {
					"message": "Updates available",
					"updateMessage": true,
					"date": moment().format('MMMM Do YYYY, HH:mm:ss'),
					"links": [
					{
						"text": "Update",
						"href": "/#/settings-updates"
					}
					]
				};
			} else {
				removeUpdateMessage();
			}
		});
	}

	function removeUpdateMessage() {
		var notifications = sessionStorage.getItem('notifications');
		if (
			notifications !== null &&
			notifications !== undefined &&
			notifications !== 'null' &&
			notifications.length > 0
		) {
			notifications = JSON.parse(notifications);
			for (var i = 0; i < notifications.length; i++) {
				if (notifications[i].updateMessage !== undefined && notifications[i].updateMessage) {
					notifications.splice(i, 1);
					sessionStorage.setItem('notifications', JSON.stringify(notifications));
					$rootScope.notifications = {};
					break;
				}
			}
		}
	}

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

		identitySrv.getPublicKeyData(vm.activeUser.id).success(function (data) {
			vm.publicKeyInfo = data;
		});

		identitySrv.checkUserKey(vm.activeUser.id).success(function (data) {
			if (data <= 1 && $rootScope.$state.current.name != 'account-settings') {
				$('.js-auto-set-key').addClass('bp-set-pub-key');
			}
			/*else {
			  $('.js-public-key-manager').remove();
			  }*/
		});
	});


	function getDelegateDocument() {
		identitySrv.getIdentityDelegateDocument().success(function (data) {
			vm.message = data;
			if (vm.message) {
				$('.bp-sign-input').addClass('bp-sign-target');
			}

			identitySrv.getCurrentUser().success(function (data) {
				vm.activeUser = data;
			});
		});
	}

	//getDelegateDocument();

	function approveDocument() {
		LOADING_SCREEN();
		identitySrv.approveIdentityDelegate(encodeURIComponent(vm.message)).success(function () {
			LOADING_SCREEN('none');
			//SweetAlert.swal("Approved!", "Message was successfully approved.", "success");
			$rootScope.notifications = {
				"message": "Message was successfully approved",
				"date": moment().format('MMMM Do YYYY, HH:mm:ss')
			};
		}).error(function (error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Error: " + error, "error");

			$rootScope.notifications = {
				"message": "Error on approve Document. " + error,
				"date": moment().format('MMMM Do YYYY, HH:mm:ss')
			};
		});
	};

	function autoSign() {
		if (vm.hasPGPplugin) {
			approveDocument();
		}
	}

	function setPublicKey(publicKey) {
		LOADING_SCREEN();
		$('#js-public-key-manager').removeClass('js-public-key-manager_show');
		identitySrv.updatePublicKey(encodeURIComponent(publicKey)).success(function (data) {
			identitySrv.createIdentityDelegateDocument().success(function () {
				LOADING_SCREEN('none');

				$rootScope.notifications = {
					"message": "Public key successfully set",
					"date": moment().format('MMMM Do YYYY, HH:mm:ss')
				};

				getDelegateDocument();
			}).error(function (error) {
				LOADING_SCREEN('none');
				SweetAlert.swal("ERROR!", "Error: " + error, "error");
			});
		}).error(function (error) {
			LOADING_SCREEN('none');
			SweetAlert.swal("ERROR!", "Error: " + error, "error");

			$rootScope.notifications = {
				"message": "Error on adding Public key. " + error,
				"date": moment().format('MMMM Do YYYY, HH:mm:ss')
			};
		});
	};
}
