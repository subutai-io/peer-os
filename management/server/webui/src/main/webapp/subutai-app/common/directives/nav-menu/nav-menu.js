'use strict';

angular.module('subutai.nav-menu', [])
        .directive('navMenu', NavMenu);

function NavMenu() {
    return {
        restrict: 'E',
        scope: {adminMenus : '=', activeState : '='},
        bindToController: true,
        templateUrl: 'subutai-app/common/partials/nav-menu.html',
        controller: ['$scope', function( $scope ) {
            $scope.adminMenus = "";
            $scope.activeState = "";
            console.log( $scope.adminMenus );
            console.log( $scope.activeState )
        }],
        controllerAs: 'dir'
    }
}
