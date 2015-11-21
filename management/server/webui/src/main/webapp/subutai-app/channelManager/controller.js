/**
 * Created by talas on 6/23/15.
 */
'use strict';

angular
    .module('subutai.channel-manager.controller', [])
    .controller('ChannelManagerCtrl', ChannelManagerCtrl);

ChannelManagerCtrl.$inject = ['channelManagerService'];

function ChannelManagerCtrl(channelManagerService) {
    var self = this;

    self.addPanel = addPanel;
    self.closePanel = closePanel;
    self.addToken = addToken;
    self.accounts = ["Admin", "Karaf", "User"];
    self.tokenToDeleteInx = 0;
    self.tokenToRemove = tokenToRemove;
    self.removeToken = removeToken;

    self.newToken = {
        token: "8f846079-b31f-41da-b3c3-2fdbe81fc49d",
        date: "2015-06-23 15:39:37.823",
        ipRangeStart: "*",
        ipRangeEnd: "*",
        status: true,
        tokenName: "some token",
        username: "Karaf",
        validPeriod: 10
    };

    self.selectedTokenInx = 0;

    self.getSelectedToken = function () {
        return self.tokens[self.selectedTokenInx];
    };

    self.tokens = [];

    getTokens();

    function addPanel(action, inx) {
        jQuery('#resizable-pane').removeClass('fullWidthPane');
        var editTokenForm = jQuery('#edit-token-form');
        var createTokenForm = jQuery('#create-token-form');
        if (action == 'createToken') {
            self.newToken = {
                token: "8f846079-b31f-41da-b3c3-2fdbe81fc49d",
                date: "2015-06-23 15:39:37.823",
                ipRangeStart: "*",
                ipRangeEnd: "*",
                status: true,
                tokenName: "",
                username: "",
                validPeriod: 10
            };
            if (editTokenForm) {
                editTokenForm.css('display', 'none');
                editTokenForm.removeClass('animated bounceOutRight bounceInRight');
            }
            if (createTokenForm) {
                createTokenForm.css('display', 'block');
                createTokenForm.removeClass('bounceOutRight');
                createTokenForm.addClass('animated bounceInRight');
            }
        }
        else if (action == 'editToken') {
            //self.selectedToken = token;
            self.selectedTokenInx = inx;
            if (createTokenForm) {
                createTokenForm.css('display', 'none');
                createTokenForm.removeClass('animated bounceOutRight bounceInRight');
            }
            if (editTokenForm) {
                editTokenForm.css('display', 'block');
                editTokenForm.removeClass('bounceOutRight');
                editTokenForm.addClass('animated bounceInRight');
            }
        }
    }

    function closePanel(action) {
        jQuery('#resizable-pane').addClass('fullWidthPane');
        if (action == 'createToken') {
            var createTokenForm = jQuery('#create-token-form');
            if (createTokenForm) {
                createTokenForm.addClass('bounceOutRight');
                createTokenForm.removeClass('animated bounceOutRight bounceInRight');
                createTokenForm.css('display', 'none');
            }
        }
        else if (action == 'editToken') {
            var editTokenForm = jQuery('#edit-token-form');
            if (editTokenForm) {
                editTokenForm.addClass('bounceOutRight');
                editTokenForm.removeClass('animated bounceOutRight bounceInRight');
                editTokenForm.css('display', 'none');
            }
        }
    }

    function tokenToRemove(inx) {
        self.tokenToDeleteInx = inx;
    }

    function addToken() {
        console.log(self.newToken);
        console.log(self.selectedAccount);
        self.tokens.push(self.newToken);
        self.closePanel('createToken');
        channelManagerService.addToken(self.newToken)
            .success(function () {
                console.log("token added");
            })
            .error(function () {

            });
    }

    function removeToken() {
        if (self.tokenToDeleteInx > -1) {
            channelManagerService.removeToken(self.tokens[self.tokenToDeleteInx])
                .success(function () {
                    console.log("removed token");
                })
                .error(function () {
                    console.error("Token removal failed");
                });
            self.tokens.splice(self.tokenToDeleteInx, 1);
            jQuery('#deleteToken').modal('toggle');
        }
    }

    function getTokens() {
        channelManagerService.getTokens()
            .success(function (data) {
                self.tokens = data;
            })
            .error(function () {
                console.error("Error occurred while retrieving data");
            }
        );
    }
}
