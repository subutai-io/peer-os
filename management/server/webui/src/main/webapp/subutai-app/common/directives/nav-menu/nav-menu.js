'use strict';

angular.module('subutai.nav-menu', [])
        .directive('navMenu', NavMenu);

function NavMenu() {
    return {
        restrict: 'E',
        scope: {adminMenus : '=adminMenus', activeState : '@activeState'},
        bindToController: true,
        templateUrl: 'subutai-app/common/partials/nav-menu.html',
        controller: ['$scope', function( $scope ) {
			console.log($scope);
            /*$scope.adminMenus = "";
            $scope.activeState = "";*/
            console.log( $scope.dir.adminMenus );
            console.log( $scope.dir.activeState )
        }],
        controllerAs: 'dir'
    }
}
