'use strict';

angular.module('subutai.nav-menu', [])
        .directive('navMenu', NavMenu);

function NavMenu() {
    return {
        restrict: 'E',
        scope: {adminMenus : '='},
        bindToController: true,
        templateUrl: 'subutai-app/common/partials/nav-menu.html',
        controller: function( ) {

        },
        controllerAs: 'dir'
    }
}
