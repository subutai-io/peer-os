'use strict';

angular.module('subutai.accountSettings.controller', [])
    .controller('AccountCtrl', AccountCtrl);


AccountCtrl.$inject = ['identitySrv', '$scope', 'DTOptionsBuilder', 'DTColumnBuilder', '$resource', '$compile', 'ngDialog', 'SweetAlert', 'cfpLoadingBar'];


function AccountCtrl(identitySrv, $scope, DTOptionsBuilder, DTColumnBuilder, $resource, $compile, ngDialog, SweetAlert, cfpLoadingBar) {

    var vm = this;

    vm.message = "That's my message!";
    vm.activeUser = {publicKey: ''};

    vm.createDelegateDocument = function () {
        identitySrv.createIdentityDelegateDocument();
    };

    vm.getDelegateDocument = function() {
        identitySrv.getIdentityDelegateDocument().success(function(data) {
            console.log(data);
            vm.message = data;
            if (vm.message) {
                $('.bp-sign-input').addClass('bp-sign-target');
            }
        });
    };

    vm.approveDocument = function () {
        identitySrv.approveIdentityDelegate(encodeURIComponent(vm.message));
    };

    vm.setPublicKey = function () {
        identitySrv.updatePublicKey(encodeURIComponent(vm.activeUser.publicKey));
    };

    identitySrv.getCurrentUser().success(function (data) {
        vm.activeUser = data;
        identitySrv.getKey(vm.activeUser.securityKeyId).success(function (key) {
            console.log(key);
            vm.activeUser.publicKey = key;
        });
    });

    identitySrv.getTokenTypes().success(function (data) {
        vm.tokensType = data;
    });
}

